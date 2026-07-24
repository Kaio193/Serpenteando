# Serpenteando - Implementation Plan

## 1. Project Overview

**Package:** `com.kaio.cobra3d`
**Architecture:** MVP (Model-View-Presenter)
**Language:** Java
**Min SDK:** 23 (Android 6.0)
**Target SDK:** 35
**Database:** SQLite with Contract pattern

---

## 2. Project Folder Structure

```
app/src/main/java/com/kaio/cobra3d/
├── model/
│   ├── db/
│   │   ├── DatabaseHelper.java          # SQLiteOpenHelper subclass
│   │   ├── Contract.java                # Table/column constants
│   │   └── DatabaseInitializer.java     # Populate DB on first run
│   ├── entity/
│   │   ├── Question.java                # Quiz question data class
│   │   ├── Tip.java                     # Tip of the day
│   │   ├── Mission.java                 # Mission challenge
│   │   ├── MissionItem.java             # Individual checkbox item
│   │   ├── MythTruth.java               # Myth/truth pair
│   │   ├── Content.java                 # Educational content (cobras, anfibios, etc.)
│   │   ├── AnatomyLayer.java            # 3D anatomy layer definition
│   │   ├── AnatomyHotspot.java          # Clickable point on 3D model
│   │   └── UserProgress.java            # User progress tracking
│   └── repository/
│       ├── QuestionRepository.java      # CRUD for questions
│       ├── TipRepository.java           # CRUD for tips
│       ├── MissionRepository.java       # CRUD for missions
│       ├── ContentRepository.java       # CRUD for educational content
│       ├── MythTruthRepository.java     # CRUD for myths/truths
│       ├── AnatomyRepository.java       # CRUD for anatomy data
│       └── ProgressRepository.java      # CRUD for user progress
├── presenter/
│   ├── HomePresenter.java               # Home screen logic
│   ├── QuizPresenter.java               # Quiz logic, scoring, difficulty
│   ├── MissionPresenter.java            # Mission logic, verification
│   ├── AnatomyPresenter.java            # 3D viewer layer logic
│   ├── ContentPresenter.java            # Educational content display
│   ├── MythTruthPresenter.java          # Myth/truth display logic
│   └── TipPresenter.java                # Random tip selection
├── view/
│   ├── MainActivity.java                # Single activity, fragment host
│   ├── HomeFragment.java                # Home/dashboard screen
│   ├── QuizFragment.java                # Quiz screen
│   ├── MissionFragment.java             # Mission mode screen
│   ├── Anatomy3DFragment.java           # 3D snake anatomy viewer
│   ├── ContentListFragment.java         # List of content categories
│   ├── ContentDetailFragment.java       # Single content detail view
│   ├── MythTruthFragment.java           # Myths and truths display
│   └── SettingsFragment.java            # Settings (voice, theme)
├── adapter/
│   ├── QuestionAdapter.java             # RecyclerView adapter for quiz
│   ├── MissionItemAdapter.java          # RecyclerView adapter for mission checkboxes
│   ├── ContentAdapter.java              # RecyclerView adapter for content list
│   ├── MythTruthAdapter.java            # RecyclerView adapter for myths/truths
│   └── HotspotAdapter.java             # Adapter for 3D hotspots
├── util/
│   ├── TTSHelper.java                   # Text-to-Speech wrapper
│   ├── SoundPlayer.java                 # Sound effects
│   ├── AnimationHelper.java             # View animations
│   ├── ResourceHelper.java              # Drawable/string resource utils
│   ├── PreferenceManager.java           # SharedPreferences wrapper
│   └── Constants.java                   # App-wide constants
└── contract/
    ├── HomeContract.java                # Home MVP interface
    ├── QuizContract.java                # Quiz MVP interface
    ├── MissionContract.java             # Mission MVP interface
    ├── AnatomyContract.java             # Anatomy MVP interface
    ├── ContentContract.java             # Content MVP interface
    └── TipContract.java                 # Tip MVP interface
```

---

## 3. Database Schema

### 3.1 Table: `questions`
```sql
CREATE TABLE questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer INTEGER NOT NULL,  -- 0=A, 1=B, 2=C, 3=D
    difficulty INTEGER NOT NULL DEFAULT 1,  -- 1=Facil, 2=Medio, 3=Dificil
    category TEXT NOT NULL,  -- 'cobras', 'anfibios', 'peconhentos', 'nao_peconhentos', 'anatomia'
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 Table: `tips`
```sql
CREATE TABLE tips (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tip_text TEXT NOT NULL,
    category TEXT,  -- 'curiosidade', 'cuidado', 'anatomia'
    is_active INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.3 Table: `missions`
```sql
CREATE TABLE missions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    difficulty INTEGER NOT NULL DEFAULT 1,
    points INTEGER NOT NULL DEFAULT 10,
    is_completed INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 Table: `mission_items`
```sql
CREATE TABLE mission_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mission_id INTEGER NOT NULL,
    item_text TEXT NOT NULL,
    is_correct INTEGER NOT NULL,  -- 1=correct characteristic, 0=wrong
    display_order INTEGER NOT NULL,
    FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);
```

### 3.5 Table: `myths_truths`
```sql
CREATE TABLE myths_truths (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    statement TEXT NOT NULL,
    is_myth INTEGER NOT NULL,  -- 1=myth, 0=truth
    explanation TEXT NOT NULL,
    category TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.6 Table: `content`
```sql
CREATE TABLE content (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category TEXT NOT NULL,  -- 'cobras', 'anfibios', 'peconhentos', 'nao_peconhentos'
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    image_res_name TEXT,  -- drawable resource name
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.7 Table: `anatomy_layers`
```sql
CREATE TABLE anatomy_layers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    layer_name TEXT NOT NULL,  -- 'pele', 'musculos', 'circulatorio', 'orgaos'
    display_name TEXT NOT NULL,
    description TEXT NOT NULL,
    model_file_name TEXT,  -- .glb file name in assets
    display_order INTEGER NOT NULL
);
```

### 3.8 Table: `anatomy_hotspots`
```sql
CREATE TABLE anatomy_hotspots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    layer_id INTEGER NOT NULL,
    hotspot_name TEXT NOT NULL,
    description TEXT NOT NULL,
    position_x REAL NOT NULL,
    position_y REAL NOT NULL,
    position_z REAL NOT NULL,
    FOREIGN KEY (layer_id) REFERENCES anatomy_layers(id) ON DELETE CASCADE
);
```

### 3.9 Table: `user_progress`
```sql
CREATE TABLE user_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_high_score INTEGER DEFAULT 0,
    quizzes_completed INTEGER DEFAULT 0,
    missions_completed INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    last_quiz_date TEXT,
    last_tip_date TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. Java Classes - Detailed Responsibilities

### 4.1 Model Layer

| Class | Responsibility |
|-------|---------------|
| `DatabaseHelper` | SQLiteOpenHelper singleton, creates/upgrades tables, enables foreign keys |
| `Contract` | Inner classes with table names and column constants |
| `DatabaseInitializer` | Seeds initial data (questions, tips, missions, content) from assets |
| `Question` | POJO: id, questionText, options[4], correctAnswer, difficulty, category, explanation |
| `Tip` | POJO: id, tipText, category, isActive |
| `Mission` | POJO: id, title, description, difficulty, points, isCompleted |
| `MissionItem` | POJO: id, missionId, itemText, isCorrect, displayOrder |
| `MythTruth` | POJO: id, statement, isMyth, explanation, category |
| `Content` | POJO: id, category, title, body, imageResName, displayOrder |
| `AnatomyLayer` | POJO: id, layerName, displayName, description, modelFileName, displayOrder |
| `AnatomyHotspot` | POJO: id, layerId, hotspotName, description, positionX/Y/Z |
| `UserProgress` | POJO: id, quizHighScore, quizzesCompleted, missionsCompleted, totalPoints, streak |
| Repositories (7) | Each handles CRUD for its entity using SQLiteDatabase |

### 4.2 Presenter Layer

| Class | Responsibility |
|-------|---------------|
| `HomePresenter` | Loads tip of the day, initializes progress display |
| `QuizPresenter` | Manages quiz state: load questions, track current question, calculate score, handle difficulty |
| `MissionPresenter` | Loads missions, validates checkbox selections, calculates points |
| `AnatomyPresenter` | Manages layer switching, hotspot selection, 3D model state |
| `ContentPresenter` | Loads content list/detail from repository |
| `MythTruthPresenter` | Loads and filters myths/truths |
| `TipPresenter` | Loads random tip, handles voice toggle |

### 4.3 View Layer

| Class | Responsibility |
|-------|---------------|
| `MainActivity` | Single activity, hosts fragments, manages navigation (BottomNavigationView) |
| `HomeFragment` | Dashboard with tip of the day, quick access buttons, progress summary |
| `QuizFragment` | Quiz UI with RadioButtons, progress bar, score display, difficulty selector |
| `MissionFragment` | Mission UI with CheckBox list, verify button, score feedback |
| `Anatomy3DFragment` | SceneView 3D viewer, layer buttons, hotspot info panel |
| `ContentListFragment` | RecyclerView of content cards by category |
| `ContentDetailFragment` | Full content view with title, body, optional image |
| `MythTruthFragment` | List of myths/truths with visual distinction (red=green) |
| `SettingsFragment` | Voice toggle, theme selection, about info |

### 4.4 Adapter Layer

| Class | Responsibility |
|-------|---------------|
| `QuestionAdapter` | Binds question data to quiz UI items |
| `MissionItemAdapter` | Binds mission items to checkbox list |
| `ContentAdapter` | Binds content cards to RecyclerView |
| `MythTruthAdapter` | Binds myth/truth items with color coding |
| `HotspotAdapter` | Manages clickable hotspot markers on 3D view |

### 4.5 Utility Layer

| Class | Responsibility |
|-------|---------------|
| `TTSHelper` | Text-to-Speech initialization, speak, stop, shutdown |
| `SoundPlayer` | MediaPlayer wrapper for correct/wrong answer sounds |
| `AnimationHelper` | Fade, slide, scale animations for UI transitions |
| `ResourceHelper` | Get drawable/resource by name dynamically |
| `PreferenceManager` | Read/write SharedPreferences (voice state, high scores) |
| `Constants` | App-wide constants (bundle keys, request codes, defaults) |

---

## 5. XML Layouts

### 5.1 Activities
| File | Content |
|------|---------|
| `activity_main.xml` | FrameLayout container + BottomNavigationView |

### 5.2 Fragments
| File | Content |
|------|---------|
| `fragment_home.xml` | Tip card, quick action buttons, progress summary card |
| `fragment_quiz.xml` | Question text, RadioGroup with 4 options, progress bar, score, submit button |
| `fragment_mission.xml` | Mission title/description, RecyclerView of checkboxes, verify button |
| `fragment_anatomy_3d.xml` | SceneView container, layer toggle buttons, info panel |
| `fragment_content_list.xml` | RecyclerView of content cards |
| `fragment_content_detail.xml` | ScrollView with title, body text, optional image |
| `fragment_myth_truth.xml` | RecyclerView of myth/truth cards |
| `fragment_settings.xml` | Voice switch, theme selector, about section |

### 5.3 RecyclerView Items
| File | Content |
|------|---------|
| `item_question.xml` | Question card with option buttons |
| `item_mission_checkbox.xml` | CheckBox with styled card |
| `item_content_card.xml` | Card with title, preview text, category badge |
| `item_myth_truth.xml` | Card with statement, truth/myth badge, explanation |

### 5.4 Menu
| File | Content |
|------|---------|
| `menu_bottom_nav.xml` | Bottom navigation: Home, Quiz, Missions, 3D, Settings |

### 5.5 Values
| File | Content |
|------|---------|
| `strings.xml` | All user-facing strings (Portuguese) |
| `colors.xml` | Color palette: primary (#2E7D32), secondary (#81C784), accent (#FFB74D), error (#E53935), background (#1B5E20) |
| `dimens.xml` | Button height (48dp), padding, margins, text sizes |
| `styles.xml` | MaterialTheme, button styles, card styles, text styles |
| `arrays.xml` | String arrays for spinners (difficulty levels, categories) |

### 5.6 Drawable
| File | Content |
|------|---------|
| `bg_screen.xml` | Gradient background |
| `card_dark.xml` | Dark card background |
| `card_soft.xml` | Light card background |
| `button_green.xml` | Primary button drawable |
| `button_red.xml` | Danger button drawable |
| `button_light.xml` | Light button drawable |
| `ic_home.xml` | Home icon |
| `ic_quiz.xml` | Quiz icon |
| `ic_mission.xml` | Mission icon |
| `ic_anatomy.xml` | 3D icon |
| `ic_settings.xml` | Settings icon |

---

## 6. Implementation Phases

### Phase 1: Project Setup & Database (Days 1-2)

**Goal:** Foundation layer with database and models

**Tasks:**
1. Create new project with package `com.kaio.cobra3d`
2. Configure `build.gradle.kts`:
   - Add Material Design dependency
   - Add Room/SQLite dependencies (or raw SQLite)
   - Configure JavaDoc generation
3. Create `Contract.java` with all table/column constants
4. Create `DatabaseHelper.java`:
   - onCreate: create all 9 tables
   - onUpgrade: drop and recreate
   - enableForeignKeys
5. Create all entity POJOs (Question, Tip, Mission, etc.)
6. Create `DatabaseInitializer.java`:
   - Seed 20+ questions across categories
   - Seed 15+ tips
   - Seed 5+ missions with items
   - Seed 10+ myths/truths
   - Seed educational content for all categories
   - Seed anatomy layers and hotspots
7. Create all Repository classes with CRUD operations

**Deliverables:** Working database with sample data

---

### Phase 2: MVP Contracts & Presenters (Days 3-4)

**Goal:** MVP interfaces and business logic

**Tasks:**
1. Create all MVP Contract interfaces:
   - `HomeContract` (View: showTip, showProgress; Presenter: loadTip, loadProgress)
   - `QuizContract` (View: showQuestion, showScore, showResult; Presenter: loadNextQuestion, checkAnswer, getScore)
   - `MissionContract` (View: showMission, showResult; Presenter: loadMission, verifySelection)
   - `AnatomyContract` (View: showLayer, showHotspotInfo; Presenter: switchLayer, selectHotspot)
   - `ContentContract` (View: showContentList, showDetail; Presenter: loadContent, loadDetail)
   - `TipContract` (View: showTip; Presenter: loadRandomTip)
2. Implement `HomePresenter`
3. Implement `QuizPresenter`:
   - Track current question index
   - Calculate score
   - Handle difficulty filtering
   - Save progress on completion
4. Implement `MissionPresenter`:
   - Load mission with items
   - Validate correct/incorrect selections
   - Calculate points
5. Implement `AnatomyPresenter`
6. Implement `ContentPresenter`
7. Implement `TipPresenter`

**Deliverables:** All presenters with business logic

---

### Phase 3: Navigation & Home Screen (Days 5-6)

**Goal:** App shell with navigation and home screen

**Tasks:**
1. Create `MainActivity.java`:
   - Host fragments in FrameLayout
   - Setup BottomNavigationView
   - Handle fragment transactions
2. Create `menu_bottom_nav.xml`
3. Create `fragment_home.xml`:
   - Tip of the day card
   - Quick action buttons (Quiz, Missions, 3D)
   - Progress summary (score, missions completed)
4. Implement `HomeFragment.java`
5. Create drawable resources (cards, buttons, icons)
6. Create `colors.xml`, `dimens.xml`, `styles.xml`
7. Configure theme in `themes.xml`

**Deliverables:** Working navigation and home screen

---

### Phase 4: Educational Content (Days 7-8)

**Goal:** Content display screens

**Tasks:**
1. Create `fragment_content_list.xml`
2. Implement `ContentAdapter.java`
3. Implement `ContentListFragment.java`
4. Create `fragment_content_detail.xml`
5. Implement `ContentDetailFragment.java`
6. Create `fragment_myth_truth.xml`
7. Implement `MythTruthAdapter.java`
8. Implement `MythTruthFragment.java`
9. Implement `ContentPresenter` and `MythTruthPresenter`
10. Add content images to drawable resources

**Deliverables:** All educational content screens working

---

### Phase 5: Quiz System (Days 9-11)

**Goal:** Interactive quiz with scoring

**Tasks:**
1. Create `fragment_quiz.xml`:
   - Question number indicator
   - Progress bar
   - Question text
   - RadioGroup with 4 styled RadioButtons
   - Submit button (min 48dp)
   - Score display
2. Implement `QuizFragment.java`
3. Enhance `QuizPresenter`:
   - Question shuffling
   - Difficulty selection (Facil/Medio/Dificil)
   - Score calculation
   - High score tracking
4. Add quiz completion dialog
5. Add explanation display after each question
6. Add sound effects (correct/wrong)
7. Update user progress in database

**Deliverables:** Fully functional quiz system

---

### Phase 6: Mission Mode (Days 12-13)

**Goal:** Progressive challenges with checkboxes

**Tasks:**
1. Create `fragment_mission.xml`:
   - Mission title and description
   - RecyclerView of CheckBox items
   - Verify button (min 48dp)
   - Score feedback
2. Create `item_mission_checkbox.xml`
3. Implement `MissionItemAdapter.java`
4. Implement `MissionFragment.java`
5. Enhance `MissionPresenter`:
   - Load mission with items
   - Validate selections (only correct items checked)
   - Calculate points
   - Track completed missions
6. Add mission completion celebration dialog
7. Add mission difficulty levels

**Deliverables:** Fully functional mission mode

---

### Phase 7: 3D Anatomy Viewer (Days 14-17)

**Goal:** SceneView 3D snake anatomy

**Tasks:**
1. Add SceneView/Filament dependencies to `build.gradle.kts`
2. Create `.glb` model files (or use placeholders):
   - `snake_skin.glb`
   - `snake_muscles.glb`
   - `snake_circulatory.glb`
   - `snake_organs.glb`
3. Create `fragment_anatomy_3d.xml`:
   - SceneView container (match_parent)
   - Layer toggle buttons (Pele, Musculos, Circulatorio, Orgaos)
   - Info panel (card with hotspot description)
4. Implement `Anatomy3DFragment.java`:
   - Initialize SceneView
   - Load 3D models from assets
   - Handle layer switching
   - Implement hotspot click detection
   - Show info panel on hotspot click
5. Implement `AnatomyPresenter`:
   - Manage current layer
   - Handle hotspot selection
   - Provide hotspot descriptions
6. Add animation for layer transitions
7. Add loading indicator for 3D models

**Deliverables:** Working 3D anatomy viewer

---

### Phase 8: Voice Mode & Settings (Days 18-19)

**Goal:** TTS integration and settings

**Tasks:**
1. Implement `TTSHelper.java`:
   - Initialize TextToSpeech
   - Speak text in Portuguese
   - Stop speaking
   - Shutdown properly
2. Create `fragment_settings.xml`:
   - Voice toggle switch
   - Theme selector (if implementing dark/light)
   - About section
3. Implement `SettingsFragment.java`
4. Integrate TTS in all content screens
5. Add TTS to quiz questions and explanations
6. Add TTS to mission descriptions
7. Add TTS to 3D anatomy descriptions
8. Implement `PreferenceManager.java`
9. Persist voice setting across sessions

**Deliverables:** Voice mode working in all screens

---

### Phase 9: Animations & Polish (Days 20-21)

**Goal:** UI polish and animations

**Tasks:**
1. Implement `AnimationHelper.java`:
   - Fade transitions between fragments
   - Slide-in animations for cards
   - Scale animations for buttons
   - Pulse animation for hotspots
2. Add transitions between screens
3. Add loading states for all screens
4. Add empty states (no questions, no missions)
5. Add error states and retry
6. Polish color contrast (verify 4.5:1 ratio)
7. Verify all buttons are min 48dp
8. Add content descriptions for accessibility
9. Test on different screen sizes

**Deliverables:** Polished UI with animations

---

### Phase 10: Testing (Days 22-24)

**Goal:** JUnit and Espresso tests

**Tasks:**
1. Create unit tests for presenters:
   - `QuizPresenterTest.java`
   - `MissionPresenterTest.java`
   - `ContentPresenterTest.java`
   - `TipPresenterTest.java`
2. Create unit tests for repositories:
   - `QuestionRepositoryTest.java`
   - `MissionRepositoryTest.java`
   - `ProgressRepositoryTest.java`
3. Create instrumented tests:
   - `MainActivityTest.java` (navigation)
   - `QuizFragmentTest.java` (quiz flow)
   - `MissionFragmentTest.java` (mission flow)
   - `AnatomyFragmentTest.java` (layer switching)
4. Add test data factories
5. Achieve >70% code coverage

**Deliverables:** Complete test suite

---

### Phase 11: Documentation & JavaDoc (Days 25-26)

**Goal:** Documentation and ABNT formatting

**Tasks:**
1. Add JavaDoc to all public classes and methods
2. Add package-info.java for each package
3. Configure JavaDoc generation in build.gradle
4. Generate JavaDoc HTML
5. Create README.md with:
   - Project description
   - Architecture overview
   - Setup instructions
   - Class diagram
6. Format any written documentation in ABNT style
7. Add inline comments for complex logic

**Deliverables:** Complete documentation

---

### Phase 12: Final Testing & Release (Days 27-28)

**Goal:** Final validation and APK generation

**Tasks:**
1. Run all unit tests
2. Run all instrumented tests
3. Test on physical device
4. Test on different API levels (23, 26, 28, 30, 33, 35)
5. Verify accessibility (TalkBack)
6. Verify performance (no ANR, smooth animations)
7. Generate signed APK
8. Create release notes

**Deliverables:** Release-ready APK

---

## 7. Testing Strategy

### 7.1 Unit Tests (JUnit 4)

**Location:** `app/src/test/java/com/kaio/cobra3d/`

**Test Categories:**

1. **Presenter Tests:**
   - QuizPresenter: question loading, answer checking, score calculation, difficulty filtering
   - MissionPresenter: mission loading, selection validation, point calculation
   - ContentPresenter: content loading, filtering by category
   - TipPresenter: random tip selection, tip rotation

2. **Repository Tests:**
   - CRUD operations for each repository
   - Database query correctness
   - Foreign key constraints
   - Data integrity

3. **Model Tests:**
   - Entity constructors and getters
   - equals/hashCode if implemented
   - Serialization if used

### 7.2 Instrumented Tests (Espresso)

**Location:** `app/src/androidTest/java/com/kaio/cobra3d/`

**Test Categories:**

1. **Navigation Tests:**
   - Bottom nav switches fragments
   - Back button behavior
   - Deep linking (if implemented)

2. **Quiz Flow Tests:**
   - Start quiz
   - Select answer
   - Submit answer
   - See result
   - Complete quiz
   - See final score

3. **Mission Flow Tests:**
   - Load mission
   - Check items
   - Verify mission
   - See result

4. **Anatomy Tests:**
   - Load 3D view
   - Switch layers
   - Click hotspot
   - See description

5. **Content Tests:**
   - Load content list
   - Click content item
   - See detail view

6. **Accessibility Tests:**
   - All buttons min 48dp
   - Content descriptions present
   - Contrast ratio verification

### 7.3 Test Data

Create `TestDataFactory.java` for generating test data:
- Random questions with valid data
- Random missions with items
- Random tips
- Mock user progress

### 7.4 Test Coverage Goals

- Presenters: >80%
- Repositories: >75%
- Utils: >90%
- Overall: >70%

---

## 8. Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // SceneView / Filament (3D)
    implementation("io.github.sceneview:sceneview:2.0.1")
    
    // SQLite (if not using raw SQLite)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

## 9. Key Conventions

### 9.1 Code Style
- **camelCase** for variables and methods
- **PascalCase** for classes
- **SCREAMING_SNAKE_CASE** for constants
- No empty catch blocks (always log or handle)
- Min button height: 48dp
- Contrast ratio: 4.5:1 minimum

### 9.2 JavaDoc Format
```java
/**
 * Brief description of the class/method.
 * 
 * <p>Detailed description if needed.</p>
 * 
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception is thrown
 * @see RelatedClass
 */
```

### 9.3 ABNT Formatting
- 1.5 line spacing
- Times New Roman 12pt
- Margins: 3cm left, 2cm right, 2cm top/bottom
- Page numbers in footer
- Heading hierarchy: Chapter > Section > Subsection

---

## 10. Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| SceneView complexity | Use placeholder 3D models initially, iterate |
| Large DB seed data | Load from JSON in assets, async initialization |
| TTS quality | Use device TTS, test on multiple devices |
| Performance on old devices | Lazy load 3D models, implement caching |
| Test flakiness | Use IdlingResources for async operations |

---

## 11. Estimated Timeline

| Phase | Duration | Cumulative |
|-------|----------|------------|
| Phase 1: Database | 2 days | Day 2 |
| Phase 2: MVP/Presenters | 2 days | Day 4 |
| Phase 3: Navigation/Home | 2 days | Day 6 |
| Phase 4: Content | 2 days | Day 8 |
| Phase 5: Quiz | 3 days | Day 11 |
| Phase 6: Missions | 2 days | Day 13 |
| Phase 7: 3D Anatomy | 4 days | Day 17 |
| Phase 8: Voice/Settings | 2 days | Day 19 |
| Phase 9: Polish | 2 days | Day 21 |
| Phase 10: Testing | 3 days | Day 24 |
| Phase 11: Documentation | 2 days | Day 26 |
| Phase 12: Final Testing | 2 days | Day 28 |

**Total: 28 working days (approximately 6 weeks)**

---

*Document generated for Serpenteando TCC - IFPB*
*Student: Kaio | Advisor: Prof. Alexandre Costa*
