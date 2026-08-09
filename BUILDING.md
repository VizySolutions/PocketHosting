# PocketHosting + Altay — como gerar o APK

Este projeto foi recuperado do APK original e adaptado para usar **Altay** como software do servidor.

## Requisitos

- Android Studio atualizado
- JDK 17 (use o JDK incorporado do Android Studio, se disponível)
- Android SDK Platform 35 instalado
- Conexão com a internet no primeiro Gradle Sync

O projeto usa Android Gradle Plugin 8.10.1 e já inclui o Gradle Wrapper utilizado pelo projeto. Use preferencialmente `gradlew`/`gradlew.bat` para reproduzir o build.

## 1. Abrir o projeto

1. Extraia o ZIP em uma pasta normal, por exemplo `C:\PocketHosting-Altay`.
2. Abra o Android Studio.
3. Clique em **File > Open** e selecione a pasta raiz do projeto, a mesma onde ficam `settings.gradle` e `build.gradle`.
4. Aguarde o **Gradle Sync**.
5. Se o Android Studio pedir o JDK, selecione **JDK 17**.
6. Se faltar o SDK 35, abra **Tools > SDK Manager > SDK Platforms**, instale **Android API 35** e sincronize novamente.

## 2. Compilar pelo Gradle Wrapper

No Linux/macOS:

```bash
./gradlew assembleDebug
```

No Windows:

```bat
gradlew.bat assembleDebug
```

## 3. Gerar APK de teste (debug)

Para testar pelo Android Studio, conecte um celular Android por USB com depuração USB ativada e clique em **Run**.

Para apenas gerar o APK de debug, use a ação de build de APK do Android Studio. O arquivo normalmente ficará em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 4. Gerar APK de release assinado

1. Abra **Build > Generate Signed Bundle / APK**.
2. Selecione **APK**.
3. Selecione o módulo **app**.
4. Escolha a chave `.jks` original, se você ainda tiver essa chave, ou crie uma nova.
5. Escolha o build type **release**.
6. Conclua em **Create/Finish**.

> Se você não tiver mais a chave usada para assinar o APK antigo, um APK assinado com uma chave nova normalmente não poderá ser instalado por cima da instalação antiga como atualização. Faça backup dos arquivos importantes antes de desinstalar a versão antiga.

## 5. Antes de publicar uma nova versão no GitHub

Edite `app/build.gradle` e aumente os valores:

```gradle
versionCode 2
versionName "1.1"
```

Em toda nova versão, o `versionCode` deve aumentar. O `versionName` deve representar a versão publicada.

Depois publique o APK como asset em um novo release de:

`VizySolutions/PocketHosting`

O próprio app consulta o release mais recente desse repositório ao entrar na interface. Se encontrar uma versão maior que `BuildConfig.VERSION_NAME`, mostra o aviso de atualização e tenta publicar uma notificação do sistema.

## 6. Teste do Altay

No celular, faça este teste na ordem:

1. Abra o PocketHosting.
2. Use a opção de instalar/atualizar o software do servidor.
3. Confirme que foi criado/baixado `Altay/Altay.phar`.
4. Confirme que o runtime PHP foi instalado normalmente.
5. Inicie o servidor.
6. Abra o console e confirme que o Altay iniciou sem erro.
7. Reinicie o app e confira se o estado/arquivos continuam corretos.

## Alterações feitas nesta recuperação

- PocketMine-MP foi substituído por Altay na interface e na lógica do servidor.
- O arquivo do servidor agora é `Altay.phar`.
- O diretório do servidor agora é `Altay/`.
- A versão estável mais recente do Altay é obtida pela API de releases do repositório `altayofficial/Altay`.
- O runtime PHP continua usando exatamente o endereço original de `pmmp/PHP-Binaries`, conforme solicitado.
- O verificador de atualizações do PocketHosting continua usando `VizySolutions/PocketHosting/releases`.
- O verificador agora consulta atualizações ao abrir a interface do app; quando encontra uma versão mais nova, mostra um diálogo e publica uma notificação quando permitido pelo Android.
- A classe de serviço foi renomeada para `AltayServerService`.
- Arquivos gerados do APK (`R.java`, `BuildConfig.java` e assinatura antiga em `META-INF`) foram removidos para que o Android Gradle Plugin os gere corretamente.
- O `applicationId`/package histórico `com.vizysolutions.pmmpmobile` foi preservado intencionalmente para não mudar a identidade do aplicativo.
