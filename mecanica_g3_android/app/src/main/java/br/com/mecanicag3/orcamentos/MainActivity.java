package br.com.mecanicag3.orcamentos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQUEST_PICK_FILE = 1001;
    private static final int REQUEST_SAVE_BACKUP = 1002;
    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;
    private String pendingBackupJson;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(true);
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("https".equals(uri.getScheme()) && "appassets.androidplatform.net".equals(uri.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                String[] accepted = params.getAcceptTypes();
                List<String> mimeTypes = new ArrayList<>();
                if (accepted != null) for (String type : accepted) if (type != null && type.contains("/") && !mimeTypes.contains(type)) mimeTypes.add(type);
                if (!mimeTypes.isEmpty()) intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toArray(new String[0]));
                try { startActivityForResult(intent, REQUEST_PICK_FILE); }
                catch (Exception e) { fileChooserCallback = null; callback.onReceiveValue(null); return false; }
                return true;
            }
        });
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");

    }

    private class AndroidBridge {
        @JavascriptInterface public void saveBackup(String json, String filename) {
            runOnUiThread(() -> {
                pendingBackupJson = json;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, filename);
                try { startActivityForResult(intent, REQUEST_SAVE_BACKUP); }
                catch (Exception e) { toast("Não foi possível abrir a opção para salvar o backup."); }
            });
        }
        @JavascriptInterface public void printPage() {
            runOnUiThread(() -> {
                try {
                    PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                    android.print.PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter("Orcamento_Mecanica_G3");
                    printManager.print("Orçamento - Mecânica G3", adapter, new PrintAttributes.Builder().build());
                } catch (Exception e) { toast("Não foi possível abrir a impressão."); }
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE) {
            if (fileChooserCallback != null) {
                Uri[] results = (resultCode == Activity.RESULT_OK && data != null) ? WebChromeClient.FileChooserParams.parseResult(resultCode, data) : null;
                fileChooserCallback.onReceiveValue(results);
                fileChooserCallback = null;
            }
        } else if (requestCode == REQUEST_SAVE_BACKUP) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null && pendingBackupJson != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out == null) throw new Exception("Não foi possível abrir o arquivo.");
                    out.write(pendingBackupJson.getBytes(StandardCharsets.UTF_8));
                    toast("Backup da Mecânica G3 salvo com sucesso.");
                    webView.evaluateJavascript("document.getElementById('notice').textContent='Backup salvo com sucesso, incluindo fotos anexadas.';", null);
                } catch (Exception e) { toast("Erro ao salvar o backup: " + e.getMessage()); }
            }
            pendingBackupJson = null;
        }
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.removeJavascriptInterface("Android"); webView.destroy(); }
        super.onDestroy();
    }
}
