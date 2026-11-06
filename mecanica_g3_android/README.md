# Mecânica G3 - Aplicativo Android (versão 4.1)

Este projeto empacota o aplicativo HTML 4.1 em um app Android com WebView e armazenamento local persistente.

## O que está incluído
- Aplicativo HTML 4.1 aprovado, incluindo QR Code PIX e backup/restauração.
- Armazenamento local via IndexedDB no domínio local do WebView.
- Seleção de fotos da galeria/arquivos.
- Restauração de backup JSON pelo seletor de arquivos Android.
- Exportação de backup JSON pelo seletor nativo de local de salvamento, incluindo fotos.
- Impressão do orçamento pelo serviço de impressão do Android.
- Ícone e nome Mecânica G3.

## Gerar o APK

### Opção A: GitHub Actions (não exige instalar Android Studio no computador)
1. Crie um repositório no GitHub e envie todos os arquivos desta pasta para a branch `main`.
2. Abra a aba **Actions** do repositório.
3. Selecione **Build APK - Mecanica G3** e clique em **Run workflow** (ou aguarde o build automático após o primeiro push).
4. Quando terminar, abra a execução concluída e baixe o artefato **Mecanica-G3-APK-debug**.
5. Extraia o ZIP do artefato. O instalador será `app-debug.apk`.
6. Transfira o APK para o Android e autorize a instalação deste arquivo quando o sistema solicitar.

### Opção B: Android Studio
1. Abra esta pasta no Android Studio.
2. Aguarde a sincronização do Gradle e a instalação do Android SDK 35, se solicitada.
3. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. O APK de teste será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Observações importantes
- O APK gerado pelo workflow é uma versão **debug**, adequada para instalação e testes. Para distribuição pública, será necessário criar uma chave de assinatura e gerar uma versão release assinada.
- Os orçamentos ficam armazenados no aparelho. Desinstalar o aplicativo pode apagar os dados locais; faça backup JSON antes de desinstalar ou trocar de aparelho.
- O projeto não envia dados para servidor. O backup é salvo no local escolhido pelo usuário.
- O SDK Android não está incluído no ZIP. O ambiente de compilação (Android Studio ou GitHub Actions) instala as ferramentas necessárias.
