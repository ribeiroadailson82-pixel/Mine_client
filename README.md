# MineClient

Client de PvP para **Minecraft 1.8.9**, implementado como mod **Forge** — com módulos clássicos de treino: alcance de ataque estendido, hitboxes, autoclicker, anti-knockback, HUD, keystrokes e ClickGUI.

> ⚠️ **Uso exclusivamente offline.** Este projeto foi feito para **singleplayer e servidores LAN privados** (treino, testes e aprendizado de modding). Não use em servidores online públicos: além de violar as regras dos servidores, estraga o jogo dos outros. O client não tem — e não terá — qualquer recurso para burlar anti-cheat.

## Módulos

| Módulo | Categoria | Descrição | Settings |
| --- | --- | --- | --- |
| **Reach** | Combate | Aumenta o alcance de ataque | Alcance: 3.0–6.0 blocos¹ |
| **Hitboxes** | Combate | Expande a hitbox das entidades na mira | Expansão: 0.0–0.5 |
| **AutoClicker** | Combate | Cliques automáticos segurando o botão esquerdo | CPS Min/Max, Em blocos |
| **Velocity** | Combate | Reduz o knockback recebido | Horizontal %, Vertical %, Explosões |
| **ToggleSprint** | Jogador | Corrida permanente | — |
| **FastPlace** | Jogador | Remove o delay de colocar blocos/usar itens | Delay: 0–4 ticks |
| **Fullbright** | Visual | Ilumina o mundo ao máximo | — |
| **HUD** | Visual | Watermark, FPS, CPS, coordenadas e lista de módulos | FPS, CPS, Coordenadas, Lista |
| **Keystrokes** | Visual | Overlay WASD + botões do mouse | X, Y |

¹ O limite de 6 blocos existe porque o servidor integrado (singleplayer/LAN) descarta ataques além dessa distância (`NetHandlerPlayServer#processUseEntity`).

## Controles

- **Right Shift** — abre a **ClickGUI** (arraste painéis, clique esquerdo ativa módulo, clique direito expande settings; sliders arrastáveis; linha "Bind" define atalho de teclado por módulo).
- **`/mineclient`** (alias `/mcl`) — comando de chat:
  - `/mcl list` — lista módulos
  - `/mcl toggle <módulo>` — liga/desliga
  - `/mcl set <módulo> <setting> <valor>` — ajusta setting
  - `/mcl bind <módulo> <tecla|none>` — define/remove atalho
  - `/mcl save` — salva a config

A config é salva automaticamente (ao fechar a GUI e ao sair do jogo) em `.minecraft/mineclient/config.json`.

## Como compilar

Requisitos: **JDK 8 a 21** e Gradle (ou use o wrapper `./gradlew`, se presente). O toolchain usa [Essential Loom](https://repo.essential.gg) + Architectury Pack200 para o Forge 1.8.9.

```bash
gradle build
```

O jar final fica em `build/libs/` (use o jar **sem** o sufixo `-dev`).

## Como instalar

1. Instale o **Forge 1.8.9** (recomendado `11.15.1.2318`) pelo instalador oficial.
2. Copie o jar para a pasta `.minecraft/mods`.
3. Abra o jogo com o perfil Forge 1.8.9 e entre em um mundo singleplayer (ou "Abrir para LAN" com amigos na mesma rede).

Para desenvolver, `gradle runClient` inicia o jogo no ambiente de dev (requer **JDK 8** para rodar o LWJGL 2 do 1.8.9).

## Arquitetura

```
com.mineclient
├── MineClient          # entrada do mod (@Mod), registra tudo
├── module/             # Module (base), Category, ModuleManager
├── modules/            # combat/, player/, render/ — os módulos em si
├── settings/           # BooleanSetting, NumberSetting, ModeSetting
├── event/              # PacketEvent (postado na thread netty)
├── network/            # PacketInterceptor (handler no pipeline netty)
├── gui/clickgui/       # ClickGUI
├── input/              # KeybindHandler (atalhos + abrir GUI)
├── command/            # /mineclient
├── config/             # ConfigManager (JSON via Gson)
└── util/               # RayTraceUtil (reach), RenderUtil, TimerUtil
```

Módulos ficam registrados no barramento de eventos do Forge apenas enquanto habilitados; cada módulo declara seus handlers com `@SubscribeEvent`. O **Reach** funciona recalculando `mc.objectMouseOver` com raio customizado (espelhando `EntityRenderer#getMouseOver`) no momento do clique e a cada frame — sem ASM/coremod.
