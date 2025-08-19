# 🌐 ConnectivityManager - Guia de Implementação

Guia completo para implementar monitoramento de conectividade no projeto UrlShortener.

## 🎯 O que é ConnectivityManager?

ConnectivityManager é a API do Android para monitorar o status da conectividade de rede, permitindo que sua aplicação responda dinamicamente a mudanças na conexão.

**Quando usar ConnectivityManager:**
- Verificar conectividade antes de fazer chamadas de API
- Mostrar indicadores visuais de status de rede
- Implementar funcionalidades offline
- Otimizar uso de dados móveis
- Melhorar experiência do usuário com feedback em tempo real

**Benefícios para o UrlShortener:**
- Evitar tentativas desnecessárias de encurtar URLs sem internet
- Mostrar status de conectividade na UI
- Implementar retry automático quando conexão retornar
- Cache inteligente baseado no status da rede

## ⚙️ Configuração

### 1. Permissões

Adicione ao `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
```

### 2. Implementação do NetworkMonitor

**data/network/NetworkMonitor.kt:**
```kotlin
package com.everpoets.urlshortener.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
            
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(hasInternet && hasValidated)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Emitir estado inicial
        trySend(isCurrentlyConnected())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.OTHER
        }
    }
}

enum class ConnectionType {
    WIFI, MOBILE, ETHERNET, OTHER, NONE
}
```

### 3. Configuração com Hilt

**di/NetworkModule.kt:**
```kotlin
package com.everpoets.urlshortener.di

import android.content.Context
import com.everpoets.urlshortener.data.network.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }
}
```

## 🚀 Integração com ViewModel

### 1. Atualizar HomeViewModel

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val shortenUrlRepository: ShortenUrlRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    private val _shortenUiState = MutableStateFlow(ShortenUiState())
    val shortenUiState: StateFlow<ShortenUiState> = _shortenUiState.asStateFlow()
    
    val isConnected = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    fun dispatchAction(action: HomeIntent) {
        when (action) {
            is HomeIntent.ClearUserMessage -> clearUserMessage()
            is HomeIntent.ShortenUrl -> handleShortenUrlAction(action.url)
            is HomeIntent.RetryConnection -> retryLastAction()
        }
    }
    
    private fun handleShortenUrlAction(url: String) {
        if (!isConnected.value) {
            _shortenUiState.update { 
                it.copy(userMessage = R.string.error_no_internet_connection) 
            }
            return
        }
        
        viewModelScope.launch {
            setLoading()
            // ... resto da implementação existente
        }
    }
    
    private fun retryLastAction() {
        // Implementar retry da última ação quando conexão retornar
    }
}
```

### 2. Atualizar ShortenUiState

```kotlin
data class ShortenUiState(
    val isLoading: Boolean = false,
    val list: List<ShortenUrlModel> = emptyList(),
    @StringRes val userMessage: Int? = null,
    val isConnected: Boolean = true,
    val connectionType: ConnectionType = ConnectionType.WIFI
)
```

## 🎨 Componentes de UI

### 1. Indicador de Conectividade

**view/components/ConnectivityIndicator.kt:**
```kotlin
@Composable
fun ConnectivityIndicator(
    isConnected: Boolean,
    connectionType: ConnectionType,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sem conexão com a internet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
```

### 2. Botão com Status de Rede

```kotlin
@Composable
fun NetworkAwareButton(
    onClick: () -> Unit,
    isConnected: Boolean,
    isLoading: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isConnected && !isLoading,
        modifier = modifier
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processando...")
            }
            !isConnected -> {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sem Internet")
            }
            else -> {
                Text(text)
            }
        }
    }
}
```

## 📱 Uso na HomeScreen

```kotlin
@Composable
fun HomeScreen(
    uiState: ShortenUiState,
    isConnected: Boolean,
    connectionType: ConnectionType,
    dispatchAction: (HomeIntent) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("URL Shortener") },
                actions = {
                    // Indicador de tipo de conexão
                    ConnectionTypeIcon(connectionType = connectionType)
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.Info, contentDescription = "Ver histórico")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Indicador de conectividade
            ConnectivityIndicator(
                isConnected = isConnected,
                connectionType = connectionType
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Input com botão network-aware
            NetworkAwareInputButton(
                onClick = { url -> dispatchAction(HomeIntent.ShortenUrl(url)) },
                isConnected = isConnected,
                isLoading = uiState.isLoading
            )
            
            // Lista de URLs
            ShortenUrlList(uiState = uiState)
        }
    }
}
```

## 🔄 Casos de Uso Avançados

### 1. Retry Automático

```kotlin
class NetworkAwareRepository @Inject constructor(
    private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor,
    private val shortenUrlDao: ShortenUrlDao
) : ShortenUrlRepository {
    
    override suspend fun doShortenUrl(url: String): Result<ShortenUrlModel> {
        return if (networkMonitor.isCurrentlyConnected()) {
            performNetworkCall(url)
        } else {
            // Salvar para retry posterior
            saveForLaterRetry(url)
            Result.failure(NoInternetException())
        }
    }
    
    private suspend fun performNetworkCall(url: String): Result<ShortenUrlModel> {
        return safeApiCall {
            apiService.shortenUrl(ShortenUrlRequest(url))
        }.fold(
            onSuccess = { response ->
                val model = response.toShortenUrlModel()
                shortenUrlDao.insertUrl(model.toShortenUrlEntity())
                Result.success(model)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
```

### 2. Cache Inteligente

```kotlin
class SmartCacheManager @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val shortenUrlDao: ShortenUrlDao
) {
    
    suspend fun getCachedUrls(): List<ShortenUrlModel> {
        return if (networkMonitor.isCurrentlyConnected()) {
            // Buscar dados frescos da API se conectado
            fetchFreshData()
        } else {
            // Usar cache local se desconectado
            shortenUrlDao.getAllUrls().map { it.toShortenUrlModel() }
        }
    }
}
```

## ✅ Boas Práticas

1. **Sempre verificar conectividade**: Antes de fazer chamadas de rede
2. **Feedback visual**: Mostrar status de conectividade ao usuário
3. **Graceful degradation**: Funcionar offline quando possível
4. **Retry inteligente**: Tentar novamente quando conexão retornar
5. **Otimizar para dados móveis**: Considerar tipo de conexão
6. **Unregister callbacks**: Sempre limpar callbacks para evitar memory leaks
7. **Estado inicial**: Sempre emitir estado inicial no Flow

## 🎯 Próximos Passos

1. Implementar `NetworkMonitor`
2. Criar `NetworkModule` no Hilt
3. Atualizar `HomeViewModel` com monitoramento
4. Adicionar componentes de UI para status de rede
5. Implementar retry automático
6. Adicionar testes para cenários offline
7. Otimizar para diferentes tipos de conexão

## 🧪 Testando Conectividade

### Simulação no Emulador
```bash
# Desabilitar dados
adb shell svc data disable

# Habilitar dados
adb shell svc data enable

# Desabilitar WiFi
adb shell svc wifi disable

# Habilitar WiFi
adb shell svc wifi enable
```

### Testes Unitários
```kotlin
@Test
fun `should show error when no internet connection`() = runTest {
    // Given
    every { networkMonitor.isCurrentlyConnected() } returns false
    
    // When
    viewModel.dispatchAction(HomeIntent.ShortenUrl("https://example.com"))
    
    // Then
    assertEquals(R.string.error_no_internet_connection, viewModel.shortenUiState.value.userMessage)
}
```
