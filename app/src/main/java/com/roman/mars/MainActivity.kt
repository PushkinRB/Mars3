package com.roman.mars
import android.Manifest import android.content.pm.PackageManager import android.os.Bundle import android.util.Log import androidx.activity.ComponentActivity import androidx.activity.compose.rememberLauncherForActivityResult import androidx.activity.compose.setContent import androidx.activity.enableEdgeToEdge import androidx.activity.result.contract.ActivityResultContracts import androidx.activity.viewModels import androidx.compose.runtime.LaunchedEffect import androidx.compose.runtime.getValue import androidx.compose.runtime.mutableStateOf import androidx.compose.runtime.setValue import androidx.core.content.ContextCompat import androidx.lifecycle.compose.collectAsStateWithLifecycle import androidx.lifecycle.lifecycleScope import com.roman.mars.data.local.RememberMeStorage import com.roman.mars.data.local.SecureCredentialsStorage import com.roman.mars.data.model.Chat import com.roman.mars.data.model.MatchedContact import com.roman.mars.data.repository.ContactMatcherRepository import com.roman.mars.data.repository.ContactRepository import com.roman.mars.data.repository.MarsUserRepository import com.roman.mars.data.repository.PrivateChatRepository import com.roman.mars.data.supabase.SupabaseProvider import com.roman.mars.presentation.contacts.ContactListViewModel import com.roman.mars.presentation.contacts.ContactListViewModelFactory import com.roman.mars.ui.auth.AuthScreen import com.roman.mars.ui.auth.AuthViewModel import com.roman.mars.ui.chat.ChatViewModel import com.roman.mars.ui.chat.SupabaseChatRoute import com.roman.mars.ui.chatlist.SupabaseChatListRoute import com.roman.mars.ui.chatlist.SupabaseChatListViewModel import com.roman.mars.ui.common.LoadingScreen import com.roman.mars.ui.contacts.ContactListScreen import com.roman.mars.ui.theme.MarsTheme import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {
    private var selectedChat by mutableStateOf<Chat?>(null)
    private var showContacts by mutableStateOf(false)

    private val authViewModel: AuthViewModel by viewModels()
    private val chatListViewModel: SupabaseChatListViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val contactListViewModel: ContactListViewModel by viewModels {
        ContactListViewModelFactory(
            contactRepository = ContactRepository(contentResolver),
            contactMatcherRepository = ContactMatcherRepository(
                marsUserRepository = MarsUserRepository(SupabaseProvider.client)
            )
        )
    }

    private val privateChatRepository by lazy {
        PrivateChatRepository(SupabaseProvider.client)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MarsTheme {
                val authState by authViewModel.uiState.collectAsStateWithLifecycle()
                val contacts by contactListViewModel.contacts.collectAsStateWithLifecycle()
                val isContactsLoading by contactListViewModel.isLoading.collectAsStateWithLifecycle()

                val rememberMeStorage = RememberMeStorage(this@MainActivity)
                val secureCredentialsStorage = SecureCredentialsStorage(this@MainActivity)

                LaunchedEffect(Unit) {
                    val rememberMeEnabled = rememberMeStorage.isRememberMeEnabled()
                    authViewModel.applyRememberMe(rememberMeEnabled)

                    if (rememberMeEnabled) {
                        authViewModel.applySavedCredentials(
                            email = secureCredentialsStorage.getEmail(),
                            password = secureCredentialsStorage.getPassword()
                        )
                    } else {
                        secureCredentialsStorage.clearCredentials()
                        authViewModel.signOut()
                    }
                }

                LaunchedEffect(
                    authState.isAuthorized,
                    authState.rememberMe,
                    authState.email,
                    authState.password
                ) {
                    if (authState.isAuthorized) {
                        if (authState.rememberMe) {
                            secureCredentialsStorage.saveCredentials(
                                email = authState.email,
                                password = authState.password
                            )
                        } else {
                            secureCredentialsStorage.clearCredentials()
                        }
                    }
                }

                LaunchedEffect(authState.isAuthorized, authState.rememberMe) {
                    if (!authState.isAuthorized && authState.rememberMe) {
                        authViewModel.applySavedCredentials(
                            email = secureCredentialsStorage.getEmail(),
                            password = secureCredentialsStorage.getPassword()
                        )
                    }
                }

                val contactsPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        contactListViewModel.loadContacts()
                        showContacts = true
                    }
                }

                fun openContacts() {
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {
                        contactListViewModel.loadContacts()
                        showContacts = true
                    } else {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }

                fun createChatFromContact(contact: MatchedContact) {
                    val marsUser = contact.marsUser ?: return

                    lifecycleScope.launch {
                        try {
                            Log.d("MainActivity", "Creating private chat with userId=${marsUser.id}, contactName=${contact.contact.name}")

                            val chatId = privateChatRepository.createPrivateChat(
                                otherUserId = marsUser.id,
                                chatTitle = contact.contact.name
                            )

                            Log.d("MainActivity", "Private chat created/opened successfully. chatId=$chatId")

                            showContacts = false
                            chatListViewModel.loadChats()

                            selectedChat = Chat(
                                id = chatId,
                                name = contact.contact.name,
                                lastMessage = "",
                                time = "",
                                unreadCount = 0,
                                isLastMessageMine = false
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to create private chat", e)
                        }
                    }
                }

                when {
                    authState.isInitializing -> {
                        LoadingScreen()
                    }

                    !authState.isAuthorized -> {
                        AuthScreen(
                            uiState = authState,
                            onEmailChanged = authViewModel::onEmailChanged,
                            onPasswordChanged = authViewModel::onPasswordChanged,
                            onRememberMeChanged = { value ->
                                rememberMeStorage.setRememberMeEnabled(value)
                                authViewModel.onRememberMeChanged(value)
                            },
                            onTogglePasswordVisibility = authViewModel::togglePasswordVisibility,
                            onSubmit = {
                                authViewModel.submit()
                            },
                            onToggleMode = authViewModel::toggleMode
                        )
                    }

                    showContacts -> {
                        ContactListScreen(
                            contacts = contacts,
                            isLoading = isContactsLoading,
                            onContactClick = { contact ->
                                createChatFromContact(contact)
                            },
                            onBackClick = {
                                showContacts = false
                            }
                        )
                    }

                    selectedChat == null -> {
                        SupabaseChatListRoute(
                            viewModel = chatListViewModel,
                            onLogoutClick = {
                                authViewModel.signOut()
                                selectedChat = null
                                showContacts = false
                            },
                            onChatClick = { chat ->
                                selectedChat = chat
                            },
                            onOpenContactsClick = {
                                openContacts()
                            }
                        )
                    }

                    else -> {
                        val currentChat = selectedChat!!

                        SupabaseChatRoute(
                            viewModel = chatViewModel,
                            chat = currentChat,
                            onBackClick = {
                                selectedChat = null
                                chatListViewModel.loadChats()
                            },
                            onAddContactClick = {
                                openContacts()
                            }
                        )
                    }
                }
            }
        }
    }
}