# 🐍 Serpenteando

Aplicativo Android educacional sobre **serpentes brasileiras** e **prevenção de acidentes**, feito como Trabalho de Conclusão de Curso do **Técnico em Informática Integrado ao Ensino Médio do IFPB, campus Santa Luzia**.

**Autores:** Kaio Dantas de Medeiros e Ebert Souza Damasceno Silva
**Orientador:** prof. Antonio Alexandre Moura Costa

---

## O que o app tem

| Seção | Conteúdo |
|---|---|
| 📚 Enciclopédia | 10 espécies: jararaca, cascavel, coral-verdadeira, jiboia, sucuri, surucucu, urutu, caninana, cobra-cipó e muçurana, com nome científico, habitat, dieta, importância médica e curiosidades |
| ❓ Quiz | 30 questões em 3 níveis de dificuldade |
| 🎯 Missões | 3 missões gamificadas com progresso e recompensas |
| ⚖️ Mitos e verdades | 15 cartões |
| 💡 Dica do dia | 85 curiosidades, com notificação diária |
| 🧊 Anatomia 3D | Modelo 3D interativo (girar e dar zoom) renderizado com Google Filament |
| 🌎 Idiomas | Português, inglês e espanhol |

O app funciona **100% offline**: todo o conteúdo fica em arquivos JSON dentro do app.

---

## Tecnologias

- **Linguagem:** Java
- **Plataforma:** Android nativo (minSdk 23, targetSdk 36)
- **Arquitetura:** MVP (Contract → Presenter → View)
- **Navegação:** Navigation Component, uma Activity com Fragments
- **3D:** Google Filament (`Serpente_3D.glb`)
- **Dados locais:** SQLite (`SQLiteOpenHelper`) e SharedPreferences
- **Notificações:** AlarmManager + NotificationCompat (dica do dia)

---

## Estrutura

```
app/src/main/
├── assets/
│   ├── content/        # serpentes, perguntas, missões, mitos e dicas (PT, EN, ES)
│   ├── images/         # fotos das 10 espécies
│   └── Serpente_3D.glb # modelo 3D
└── java/com/serpenteando/cobra3d/
    ├── contract/       # contratos do MVP
    ├── presenter/      # regras de cada tela
    ├── view/           # Activities, Fragments e o renderizador 3D
    ├── model/          # banco SQLite e modelos
    ├── adapter/        # listas (RecyclerView)
    ├── receiver/       # alarme da dica do dia
    └── util/           # idioma, notificações, preferências
```

---

## Testes

Dos **15 casos de teste funcional** planejados no TCC, **13 foram executados e aprovados**.

---

## Como rodar

1. Instale o **Android Studio** (com JDK 17).
2. Clone o repositório:
   ```bash
   git clone https://github.com/Kaio193/Serpenteando.git
   ```
3. Abra a pasta no Android Studio e espere o Gradle sincronizar.
4. Rode num emulador ou celular com Android 6.0 ou mais novo, ou gere o APK:
   ```bash
   ./gradlew assembleDebug
   ```
