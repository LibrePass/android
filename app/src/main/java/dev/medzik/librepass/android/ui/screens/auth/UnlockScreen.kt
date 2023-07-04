package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import dev.medzik.android.cryptoutils.KeyStoreUtils
import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Type
import dev.medzik.libcrypto.EncryptException
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputField
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.utils.Biometric
import dev.medzik.librepass.android.utils.Navigation.navigate
import dev.medzik.librepass.android.utils.Remember.rememberLoadingState
import dev.medzik.librepass.android.utils.Remember.rememberSnackbarHostState
import dev.medzik.librepass.android.utils.Remember.rememberStringData
import dev.medzik.librepass.android.utils.UserDataStoreSecrets
import dev.medzik.librepass.android.utils.writeUserSecrets
import dev.medzik.librepass.client.utils.Cryptography
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.generateKeyPairFromPrivate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun UnlockScreen(navController: NavController) {
    // context must be FragmentActivity to show biometric prompt
    val context = LocalContext.current as FragmentActivity

    val snackbarHostState = rememberSnackbarHostState()
    val scope = rememberCoroutineScope()

    var loading by rememberLoadingState()
    val password = rememberStringData()

    val credentials = context.getRepository().credentials.get()!!

    fun onUnlock(password: String) {
        // disable button
        loading = true

        lateinit var privateKey: String

        scope.launch(Dispatchers.IO) {
            try {
                loading = true

                // compute base password hash
                val passwordHash = computePasswordHash(
                    password = password,
                    email = credentials.email,
                    argon2Function = Argon2(
                        32,
                        credentials.parallelism,
                        credentials.memory,
                        credentials.iterations,
                        Argon2Type.ID,
                        credentials.version,
                    )
                )

                val keyPair = generateKeyPairFromPrivate(passwordHash)

                if (keyPair.publicKey != credentials.publicKey)
                    throw EncryptException("Invalid password")

                privateKey = keyPair.privateKey
            } catch (e: EncryptException) {
                // if password is invalid
                loading = false
                snackbarHostState.showSnackbar(context.getString(R.string.Error_InvalidCredentials))
            } finally {
                val secretKey = Cryptography.computeSharedKey(privateKey, credentials.publicKey)

                context.writeUserSecrets(
                    UserDataStoreSecrets(
                        privateKey = privateKey,
                        secretKey = secretKey
                    )
                )

                // run only if loading is true (if no error occurred)
                if (loading) {
                    scope.launch(Dispatchers.Main) {
                        navController.navigate(
                            screen = Screen.Dashboard,
                            disableBack = true
                        )
                    }
                }
            }
        }
    }

    fun showBiometric() {
        Biometric.showBiometricPrompt(
            context = context,
            cipher = KeyStoreUtils.initCipherForDecryption(
                alias = Biometric.PrivateKeyAlias,
                initializationVector = credentials.biometricProtectedPrivateKeyIV!!,
                requireAuthentication = true
            ),
            onAuthenticationSucceeded = { cipher ->
                val privateKey = KeyStoreUtils.decrypt(cipher, credentials.biometricProtectedPrivateKey!!)

                val secretKey = Cryptography.computeSharedKey(privateKey, credentials.publicKey)

                runBlocking {
                    context.writeUserSecrets(
                        UserDataStoreSecrets(
                            privateKey = privateKey,
                            secretKey = secretKey
                        )
                    )
                }

                navController.navigate(
                    screen = Screen.Dashboard,
                    disableBack = true
                )
            },
            onAuthenticationFailed = { }
        )
    }

    LaunchedEffect(scope) {
        if (credentials.biometricEnabled)
            showBiometric()
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.TopBar_Unlock))
        },
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputField(
                label = stringResource(R.string.InputField_Password),
                value = password.value,
                onValueChange = { password.value = it },
                hidden = true,
                keyboardType = KeyboardType.Password
            )

            Button(
                onClick = { onUnlock(password.value) },
                enabled = password.value.isNotEmpty() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 80.dp)
            ) {
                if (loading)
                    LoadingIndicator(animating = true)
                else
                    Text(stringResource(R.string.Button_Unlock))
            }

            if (credentials.biometricEnabled) {
                OutlinedButton(
                    onClick = { showBiometric() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .padding(horizontal = 80.dp)
                ) {
                    Text(stringResource(R.string.Button_UseBiometric))
                }
            }
        }
    }
}
