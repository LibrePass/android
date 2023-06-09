package dev.medzik.librepass.android.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import dev.medzik.librepass.android.R
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CipherListItem(
    cipher: Cipher,
    openBottomSheet: (sheetContent: @Composable () -> Unit) -> Unit,
    closeBottomSheet: () -> Unit,
    itemClick: (Cipher) -> Unit,
    itemEdit: (Cipher) -> Unit,
    itemDelete: (Cipher) -> Unit
) {
    val scope = rememberCoroutineScope()

    fun showSheet() {
        scope.launch {
            openBottomSheet {
                CipherListItemSheetContent(
                    cipher = cipher,
                    view = itemClick,
                    edit = itemEdit,
                    delete = itemDelete,
                    closeBottomSheet = closeBottomSheet
                )
            }
        }
    }

    Card(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { itemClick(cipher) },
                    onLongClick = { showSheet() }
                )
                .padding(vertical = 8.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val domain = (
                if ((cipher.loginData?.uris?.size ?: 0) > 0)
                    cipher.loginData!!.uris!![0]
                else
                    "https://librepass.medzik.dev"
                )
            AsyncImage(
                model = "https://librepass-api.medzik.dev/api/cipher/icon?domain=$domain",
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val name = if (cipher.loginData!!.name.length > 16) {
                    cipher.loginData!!.name.substring(0, 16) + "..."
                } else {
                    cipher.loginData!!.name
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )

                if (cipher.loginData!!.username != null) {
                    val username =
                        if (cipher.loginData!!.username!!.length > 20)
                            cipher.loginData!!.username!!.substring(0, 20) + "..."
                        else
                            cipher.loginData!!.username!!

                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(
                onClick = { showSheet() }
            ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = null)
            }
        }
    }
}

@Composable
fun imageFromURL() {
    // on below line we are creating a column,
    Column(
        // in this column we are adding modifier
        // to fill max size, mz height and max width
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth()
            // on below line we are adding
            // padding from all sides.
            .padding(10.dp),
        // on below line we are adding vertical
        // and horizontal arrangement.
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // on below line we are adding image for our image view.
        Image(
            // on below line we are adding the image url
            // from which we will  be loading our image.
            painter = rememberAsyncImagePainter("https://media.geeksforgeeks.org/wp-content/uploads/20210101144014/gfglogo.png"),

            // on below line we are adding content
            // description for our image.
            contentDescription = "gfg image",

            // on below line we are adding modifier for our
            // image as wrap content for height and width.
            modifier = Modifier
                .wrapContentSize()
                .wrapContentHeight()
                .wrapContentWidth()
        )
    }
}

@Composable
fun CipherListItemSheetContent(
    cipher: Cipher,
    view: (Cipher) -> Unit,
    edit: (Cipher) -> Unit,
    delete: (Cipher) -> Unit,
    closeBottomSheet: () -> Unit
) {
    Column {
        TextButton(
            onClick = {
                view(cipher)
                closeBottomSheet()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.CipherBottomSheet_View),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        TextButton(
            onClick = {
                edit(cipher)
                closeBottomSheet()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.CipherBottomSheet_Edit),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        TextButton(
            onClick = {
                delete(cipher)
                closeBottomSheet()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.CipherBottomSheet_Delete),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CipherListItemPreview() {
    LazyColumn {
        item {
            CipherListItem(
                cipher = Cipher(
                    id = UUID.randomUUID(),
                    owner = UUID.randomUUID(),
                    type = CipherType.Login,
                    loginData = CipherLoginData(
                        name = "Name",
                        username = "Username"
                    )
                ),
                openBottomSheet = {},
                closeBottomSheet = {},
                itemClick = {},
                itemEdit = {},
                itemDelete = {}
            )
        }
        item {
            CipherListItem(
                cipher = Cipher(
                    id = UUID.randomUUID(),
                    owner = UUID.randomUUID(),
                    type = CipherType.Login,
                    loginData = CipherLoginData(
                        name = "Some long name of the cipher",
                        username = "Some long username of the cipher"
                    )
                ),
                openBottomSheet = {},
                closeBottomSheet = {},
                itemClick = {},
                itemEdit = {},
                itemDelete = {}
            )
        }
        item {
            CipherListItem(
                cipher = Cipher(
                    id = UUID.randomUUID(),
                    owner = UUID.randomUUID(),
                    type = CipherType.Login,
                    loginData = CipherLoginData(
                        name = "Name"
                    )
                ),
                openBottomSheet = {},
                closeBottomSheet = {},
                itemClick = {},
                itemEdit = {},
                itemDelete = {}
            )
        }
    }
}
