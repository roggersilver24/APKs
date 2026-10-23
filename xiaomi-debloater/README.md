# Xiaomi Debloater — Desenvolvimento pausado

> **EM ESPERA:** este aplicativo foi engavetado por solicitação do responsável. Preservar o código existente, sem novas funcionalidades nem publicações até nova orientação.

## Última versão experimental: v0.2

APK para Xiaomi, Redmi e POCO (MIUI/HyperOS), escrito em Kotlin + Jetpack Compose. **Esta versão ainda não foi testada em telefones físicos** e não promete compatibilidade com todos os modelos.

## Implementado
- Scanner local de aplicativos instalados, busca e filtros.
- Categorias: protegido, revisar, desconhecido e usuário.
- Conexão com Shizuku 13, autorização explícita e UserService + AIDL.
- Desativação **reversível** (`pm disable-user --user 0`) e restauração (`pm enable --user 0`), somente com confirmação individual.
- Regras de segurança duplicadas no processo privilegiado: apenas `com.miui.analytics`, `com.miui.msa.global` e `com.xiaomi.mipicks` podem ser alterados, após revisão manual. Não há remoção, root automático ou comandos arbitrários.
- Verificação do resultado e histórico local de operações confirmadas.
- Alterações desabilitadas em fabricantes não reconhecidos e perfis Android secundários.

## Como testar
1. Faça backup do aparelho e **não comece em um telefone de uso crítico**.
2. Instale o Shizuku oficial: https://shizuku.rikka.app/download/
3. Inicie o Shizuku com Depuração sem fio (Android 11+, quando suportado) ou ADB pelo computador. A versão e a ROM podem impor outras restrições.
4. Instale o APK debug da execução **Build Xiaomi Debloater Scanner** no GitHub Actions, abra o app e pressione **Autorizar / Conectar**.
5. Somente para um pacote listado como **Revisar**, leia a confirmação e, se decidir, desative-o. Confira o comportamento do aparelho.
6. Para recuperar, abra o app e use **Restaurar aplicativo**. Se o aparelho ou Shizuku estiver indisponível, pelo computador com ADB:
   ```sh
   adb shell pm enable --user 0 com.miui.msa.global
   ```
   Use o nome exato do pacote anteriormente desativado.

## Compilar
JDK 17, Android SDK 35:
```sh
cd xiaomi-debloater
./gradlew testDebugUnitTest lintDebug assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Avisos
- **Não desinstala apps.** Só desativa/reativa pacotes da lista fechada.
- A classificação `Revisar` não garante que um pacote é seguro em todas as ROMs.
- O Shizuku iniciado por ADB normalmente precisa ser reiniciado após reinicializar o aparelho.
- O inventário utiliza `QUERY_ALL_PACKAGES`, permissão restrita na Google Play; rever política antes da distribuição.
- O histórico fica no telefone e pode se perder se os dados do app forem apagados.
- Operações podem ser recusadas por MIUI/HyperOS e o app não contorna essas restrições.
- Nenhuma lista ou dado de apps é enviado a um servidor.

Referências: https://github.com/RikkaApps/Shizuku-API ; https://github.com/fadeltd/xiaomi-debloater ; https://github.com/ovsky/hyperos-debloater ; https://github.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation
