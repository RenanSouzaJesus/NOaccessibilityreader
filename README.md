# NO - Accessibility Reader

Projeto Android simples para testar leitura da tela ativa usando AccessibilityService.

## O que faz
- abre as configurações de acessibilidade do Android;
- lê a árvore acessível da janela ativa;
- captura:
  - package name do app atual;
  - node.text;
  - contentDescription;
  - viewIdResourceName;
- salva a última leitura localmente;
- mostra o conteúdo dentro do app NO.

## Como testar
1. Abra este projeto no Android Studio.
2. Execute no celular.
3. Toque em "Ativar acessibilidade".
4. Nas configurações do Android, ative "NO - Leitor de tela".
5. Abra o app que você quer testar.
6. Navegue até a tela desejada.
7. Volte para o NO e toque em "Atualizar última leitura".

## Importante
O AccessibilityService só consegue ler o que o outro app expõe para a camada
de acessibilidade do Android. Se determinado texto não estiver na árvore
acessível, ele não aparecerá aqui. Nesse caso, OCR/screenshot seria um fallback.
