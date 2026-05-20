# Guía rápida para explicar el código al profe

> Léetela en orden. Las secciones son cortas para que puedas mirarlas en el móvil mientras vas a clase.
> Si quieres preguntar a Claude algo concreto, abre claude.ai en el móvil, pega el trozo de código que te pregunten, y la conversación tendrá contexto.

---

## 1. Qué es la app (en una frase)

App Android que permite **poner marcadores en un mapa de Google**, **guardarlos en una base de datos en la nube (Supabase)**, **subir una foto** a cada uno, **listarlos**, **editarlos** y **borrarlos**.

### Tecnologías que usa
- **Kotlin + Jetpack Compose** → la interfaz (no hay XML)
- **Google Maps Compose** → el mapa
- **Supabase** → base de datos PostgreSQL en la nube + almacenamiento de imágenes
- **Arquitectura Limpia (Clean Architecture)** → el código se divide en 3 capas: `data`, `domain`, `ui`
- **MVVM** → cada pantalla tiene un ViewModel que guarda su estado
- **Corrutinas + StateFlow** → para operaciones asíncronas y estado reactivo

---

## 2. Estructura de carpetas (importante para ubicarte)

```
com.example.mapsapp/
├── MainActivity.kt          → arranca la app
├── MapsApp.kt               → crea el cliente de Supabase al iniciar
│
├── data/                    → CAPA DE DATOS (cómo se guardan/cargan)
│   ├── permissions/         → enums de permisos (Location, Camera)
│   ├── supabase/            → cliente que habla con Supabase
│   │   └── models/          → MarkerEntity (cómo es la fila en BD)
│   └── repository/          → MarkerRepositoryImpl + Mapper
│
├── domain/                  → CAPA DE NEGOCIO (la lógica "pura")
│   ├── model/Marker.kt      → modelo del marcador (sin saber nada de BD)
│   ├── repository/          → interfaz MarkerRepository (contrato)
│   └── usecase/marker/      → 4 casos de uso: Get, Insert, Update, Delete
│
└── ui/                      → CAPA DE PRESENTACIÓN (lo que ves)
    ├── navigation/          → rutas y NavigationWrapper
    ├── screens/             → 4 pantallas + sus ViewModels
    ├── components/          → DrawerMenu + PermissionManager
    └── theme/               → colores y tipografía
```

**¿Por qué 3 capas?** Para que **cada capa solo conozca a la siguiente**:
- `ui` llama a `domain` (use cases)
- `domain` llama a `data` (repository)
- `data` habla con Supabase

Si mañana cambias Supabase por SQLite, **solo tocas la capa `data`**, las otras no se enteran.

---

## 3. Cómo se conecta todo (flujo de un marcador)

Esto es lo que tienes que tener clarísimo:

```
MapScreen (UI)
   ↓ usuario hace long-click en el mapa
NavigationWrapper navega a → AddMarkerScreen
   ↓ usuario escribe título y guarda
AddMarkerViewModel.saveMarker()
   ↓ llama a
InsertMarkerUseCase()
   ↓ llama a
MarkerRepositoryImpl.insertMarker()
   ↓ llama a
MapsSupabaseClient.insertMarker()
   ↓ HTTPS
Supabase (tabla "markers" + bucket "marker-images")
```

Al volver: `onMarkerSaved` → `mapViewModel.loadMarkers()` recarga la lista y el mapa se redibuja.

---

## 4. Las preguntas que te va a hacer el profe (con respuesta lista)

### "¿Cómo eliminas un marcador?"

Tienes dos sitios donde se elimina:

**Desde la LISTA (deslizar):**
1. En `MarkerListScreen` cada item está envuelto en un `SwipeToDismissBox`.
2. Cuando deslizas derecha→izquierda, llama a `viewModel.deleteMarker(id)`.
3. En `MapViewModel.deleteMarker()` hace **UI optimista**: primero quita el marcador del estado `_markers` (desaparece al instante de la pantalla) y **después** llama a Supabase en background.
4. Si Supabase falla, recarga toda la lista (el marcador "reaparece").

**Desde el DETALLE (botón eliminar):**
1. En `MarkerDetailScreen` hay un botón rojo "Eliminar marcador".
2. Llama a `viewModel.deleteMarker()` → `DeleteMarkerUseCase` → `MarkerRepositoryImpl.deleteMarker(id)` → `MapsSupabaseClient.deleteMarker(id)`.
3. Eso ejecuta en Supabase: `DELETE FROM markers WHERE id = ?`.

---

### "¿Cómo actualizas un marcador? ¿Machacas el anterior?"

**Sí, se machaca con un `UPDATE` (no se borra y se crea de nuevo).**

1. En `MarkerDetailScreen` el usuario cambia título / descripción / imagen.
2. Pulsa "Guardar canvis" → llama a `MarkerDetailViewModel.updateMarker()`.
3. Ese ViewModel llama a `UpdateMarkerUseCase(id, title, description, imageBytes)`.
4. El repositorio:
   - **Si hay imagen nueva** (`imageBytes != null`): la sube a Supabase Storage y obtiene la URL nueva.
   - **Si no hay imagen nueva**: pasa `imageUrl = null` y NO toca el campo `image` en la BD (mantiene la anterior).
5. En `MapsSupabaseClient.updateMarker()` se ejecuta:
   ```kotlin
   client.from("markers").update({
       set("title", title)
       set("description", description)
       if (imageUrl != null) set("image", imageUrl)  // solo si hay nueva
   }) { filter { eq("id", id) } }
   ```
   Esto es un `UPDATE markers SET ... WHERE id = ?` de toda la vida.

**Importante:** **el id NUNCA cambia**, se mantiene el mismo registro en la BD; solo se sobreescriben los campos.

---

### "¿Cómo pides permisos de ubicación?"

Pasa por **3 ficheros**:

1. **`AppPermission.kt`**: una sealed class con `Location` (lista con coarse + fine) y `Camera`. Es solo el "enum" de qué permiso queremos pedir.

2. **`PermissionManager.kt`** (composable): es la lógica que:
   - Al primer render comprueba con `ContextCompat.checkSelfPermission()` si ya estaba concedido.
   - Si no, ofrece la función `requestPermissions()` que lanza el diálogo nativo de Android (`rememberLauncherForActivityResult` con `RequestMultiplePermissions`).
   - Tras la respuesta del usuario, distingue 3 casos con `shouldShowRequestPermissionRationale()`:
     - **Granted** → todo concedido
     - **Denied** → denegado pero se puede volver a pedir
     - **PermanentlyDenied** → denegó "no volver a preguntar", solo se arregla desde Ajustes

3. **`PermissionContent.kt`**: la UI que se muestra según el estado (spinner, botón de "Tornar a intentar", o botón que abre Ajustes con `Intent(ACTION_APPLICATION_DETAILS_SETTINGS)`).

En `MapScreen` se enlaza todo:
```kotlin
val permissionManager = rememberPermissionManager(AppPermission.Location)
LaunchedEffect(permissionManager.status) {
    if (status == Unknown) permissionManager.requestPermissions()
    viewModel.onPermissionResult(status)
}
```

Y según el estado del ViewModel (`MapPermissionState`), se muestra **el mapa** o **la pantalla de permiso denegado**.

---

### "¿Por qué usas Repository + UseCases? ¿No es exagerado?"

Cuatro razones:
1. **Separación**: el ViewModel no sabe que existe Supabase. Si cambias de BD, no tocas el ViewModel.
2. **Una cosa por clase**: cada UseCase hace UNA operación (`InsertMarkerUseCase`, `DeleteMarkerUseCase`...). Principio de responsabilidad única.
3. **Testeable**: puedes hacer un mock de `MarkerRepository` y testear el ViewModel sin acceder a Supabase.
4. **Reutilizable**: `GetAllMarkersUseCase` lo usan TANTO `MapViewModel` como `MarkerDetailViewModel`.

---

### "¿Qué es Supabase y cómo te conectas?"

**Supabase** = un servicio en la nube que te da:
- Una base de datos PostgreSQL
- Un almacenamiento de ficheros (Storage, tipo "carpeta" en la nube)
- Una API REST automática para hablar con ambos

**Conexión paso a paso:**

1. **Credenciales** (`local.properties`, fuera del repo):
   ```
   supabaseUrl=https://xxxxx.supabase.co
   supabaseKey=eyJh...
   ```
   En `build.gradle.kts` se leen y se exponen como `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_KEY`.

2. **Inicialización** (`MapsApp.onCreate()`):
   ```kotlin
   database = MapsSupabaseClient(
       supabaseUrl = BuildConfig.SUPABASE_URL,
       supabaseKey = BuildConfig.SUPABASE_KEY
   )
   ```

3. **El cliente** (`MapsSupabaseClient.kt`) crea con `createSupabaseClient { install(Postgrest); install(Storage) }`. A partir de ahí, usa:
   - `client.from("markers").select()` → SELECT
   - `client.from("markers").insert(entity)` → INSERT
   - `client.from("markers").update({...}) { filter {...} }` → UPDATE
   - `client.from("markers").delete { filter {...} }` → DELETE
   - `client.storage.from("marker-images").upload(name, bytes)` → subir imagen
   - `.publicUrl(name)` → URL pública de la imagen

**Tabla `markers` en Supabase:**
| campo | tipo |
|---|---|
| id | int (PK, auto) |
| title | text |
| description | text? |
| latitude | float |
| longitude | float |
| image | text? (URL) |

---

### "¿Cómo navegas entre pantallas?"

Usa **Navigation 3** (la versión nueva) con un `BackStack`:

1. **Rutas** en `Routes.kt` (una sealed class):
   - `MapScreen` (data object)
   - `AddMarkerScreen(latitude, longitude)` (data class con parámetros)
   - `MarkerListScreen` (data object)
   - `MarkerDetailScreen(markerId)` (data class)

2. **Pila** en `NavigationWrapper.kt`:
   ```kotlin
   val backStack = rememberNavBackStack(Routes.MapScreen)
   ```
   - Para ir adelante: `backStack.add(Routes.AddMarkerScreen(lat, lng))`
   - Para volver atrás: `backStack.removeLastOrNull()`
   - Para cambiar de sección desde el drawer: `backStack.clear()` + `backStack.add(...)`

3. **NavDisplay** observa el backStack y renderiza la última ruta:
   ```kotlin
   NavDisplay(backStack = backStack, entryProvider = { route -> when(route) { ... } })
   ```

4. **Paso de datos**: como las rutas son data classes, los parámetros (lat, lng, markerId) viajan dentro de la ruta misma.

---

### "¿Cómo gestionas el estado del mapa y de los marcadores?"

Con **`StateFlow`** en los ViewModels + `collectAsStateWithLifecycle()` en los Composables:

```kotlin
// MapViewModel
private val _markers = MutableStateFlow<List<Marker>>(emptyList())
val markers: StateFlow<List<Marker>> = _markers

// MapScreen
val markers by viewModel.markers.collectAsStateWithLifecycle()
```

Cuando `_markers.value` cambia, **Compose redibuja solo** los Markers del mapa. No tienes que llamar a `notifyDataSetChanged` ni nada parecido.

**¿Y la cámara del mapa (zoom/posición)?** Eso lo lleva `rememberCameraPositionState`, que es estado **del propio Composable**, no del ViewModel.

---

### "¿Por qué usas corrutinas?"

Porque las operaciones de red (Supabase) **no pueden ir en el hilo principal** (UI), si no la app se congela.

Cada vez que llamamos a un UseCase desde el ViewModel:
```kotlin
viewModelScope.launch(Dispatchers.IO) {
    insertMarkerUseCase(marker, imageBytes)
        .onSuccess { _navigateBack.value = true }
        .onFailure { it.printStackTrace() }
}
```
- `viewModelScope` → la corrutina muere si el ViewModel muere (no fuga de memoria).
- `Dispatchers.IO` → la operación se hace en un hilo de E/S, no en el UI.
- `Result` → Kotlin nativo, encapsula éxito/fallo en lugar de try/catch.

---

### "¿Cómo gestionas las imágenes (cámara y galería)?"

**Desde la cámara:**
1. El usuario pulsa "Càmera" → se pide permiso `CAMERA`.
2. Se crea un fichero temporal en `context.cacheDir`.
3. **Se obtiene una URI segura con `FileProvider`** (esto es obligatorio desde Android 7+, no se pueden compartir paths directos).
4. Se lanza `ActivityResultContracts.TakePicture()` con esa URI → se abre la cámara nativa que escribe en el fichero.
5. Al volver, leemos el fichero, lo convertimos a `Bitmap`, lo **comprimimos a JPEG 80%**, y obtenemos un `ByteArray`.

**Desde la galería:**
1. Se lanza `ActivityResultContracts.GetContent()` con tipo `image/*`.
2. El usuario elige una foto y obtenemos su `Uri`.
3. Misma compresión a JPEG 80% → `ByteArray`.

**Subida a Supabase:**
- El `ByteArray` viaja por las capas hasta `MapsSupabaseClient.uploadImage()`.
- Allí se sube al bucket `marker-images` con un nombre único (timestamp + UUID) y `upsert = true`.
- Devuelve la **URL pública** que se guarda en el campo `image` de la fila.

El `FileProvider` está declarado en el **`AndroidManifest.xml`** y la configuración de qué carpetas comparte está en `res/xml/file_paths.xml`.

---

## 5. Detalles técnicos por si pregunta cosas raras

### "¿Qué es `operator fun invoke()`?"
Es un truco de Kotlin para poder llamar al objeto como si fuera una función:
```kotlin
class DeleteMarkerUseCase { suspend operator fun invoke(id: Int) = ... }

// En vez de useCase.execute(id) escribes:
useCase(id)
```

### "¿Qué es una sealed class?"
Una clase "cerrada": **solo puede tener las subclases que tú declares en el fichero**. Sirve para hacer `when` exhaustivos. La usas en:
- `Routes` (las rutas posibles son finitas)
- `PermissionStatus` (Granted/Denied/PermanentlyDenied/Unknown)
- `MapPermissionState` (Requesting / ShowDenied / ShowPermanentlyDenied / NavigateToMap)

### "¿Qué hace el mapper?"
Convierte entre `MarkerEntity` (modelo de la BD, con anotaciones `@Serializable`) y `Marker` (modelo del dominio, sin nada de Supabase). Sirve para que la capa `domain` no dependa de Supabase.

### "¿Por qué la app cobra Application?"
Porque queremos que el cliente de Supabase **se cree una sola vez** al arrancar la app, y esté disponible en todas las pantallas. `Application.onCreate()` se ejecuta antes que cualquier Activity.

### "¿Qué es `viewModelScope`?"
Un `CoroutineScope` que vive lo mismo que el ViewModel. Si destruyes el ViewModel, todas sus corrutinas se cancelan automáticamente. Evita fugas de memoria.

### "¿Qué es `LaunchedEffect`?"
Un composable que ejecuta código **cuando entra en la composición** o cuando cambia una "key". Lo usamos para:
- Pedir permisos al primer render.
- Disparar la navegación atrás cuando `_navigateBack.value = true`.

### "¿Por qué hay 2 ViewModels para crear/editar (Add y Detail)?"
Porque tienen lógica diferente:
- `AddMarkerViewModel`: empieza vacío, recibe lat/lng del mapa, hace INSERT.
- `MarkerDetailViewModel`: recibe un `markerId`, **carga el marcador existente** de Supabase para precargar el formulario, y hace UPDATE o DELETE.

---

## 6. Recordatorio del flujo "Crear marcador" completo (por si te lo preguntan en orden)

1. Abres app → `MainActivity` → `MapsApp.onCreate()` crea cliente Supabase → muestra `MapScreen`.
2. `MapScreen` pide permiso de ubicación con `PermissionManager`.
3. `MapViewModel.loadMarkers()` llama a `GetAllMarkersUseCase` → carga marcadores existentes.
4. Long-click en el mapa → `backStack.add(Routes.AddMarkerScreen(lat, lng))`.
5. Rellenas título, descripción, capturas foto → bytes JPEG en memoria.
6. Pulsas "Guardar marcador" → `AddMarkerViewModel.saveMarker(lat, lng)`.
7. `InsertMarkerUseCase` → `MarkerRepositoryImpl.insertMarker(marker, imageBytes)`.
8. Si hay imagen: `MapsSupabaseClient.uploadImage(bytes)` → devuelve URL.
9. `marker.copy(image = url).toEntity()` → `client.from("markers").insert(entity)` → INSERT en PostgreSQL.
10. Vuelve OK → `_navigateBack.value = true` en el ViewModel.
11. `LaunchedEffect` en `AddMarkerScreen` ve el cambio → llama `onMarkerSaved { mapViewModel.loadMarkers() }` → `navigateBack()`.
12. `MapViewModel.loadMarkers()` recarga la lista → `StateFlow` cambia → Compose redibuja el mapa con el pin nuevo.

---

## 7. Cosas que NO hace la app (por si te las preguntan)

- **No tiene login** (Supabase tiene módulo de Auth pero no se usa aquí).
- **No usa Hilt/Koin** (inyección de dependencias). Cada UseCase crea su Repository directamente. Es más simple pero menos testeable.
- **No tiene base de datos local** (no usa Room). Si no hay internet, no carga marcadores.
- **No filtra ni busca**. Siempre descarga TODOS los marcadores. Para mostrar el detalle de uno, descarga toda la lista y filtra en memoria (`markers.find { it.id == markerId }`).
- **No tiene tests**.

---

## 8. Mini-glosario por si te suelta una palabra y te quedas en blanco

| Palabra | Significado en este proyecto |
|---|---|
| **Composable** | Función con `@Composable` que dibuja UI con Jetpack Compose |
| **StateFlow** | Variable observable; cuando cambia, la UI se redibuja |
| **MutableStateFlow** | Versión interna del ViewModel donde sí puedes escribir |
| **suspend** | Función que puede pausarse y reanudarse (corrutina) |
| **viewModelScope** | Scope de corrutinas que vive lo mismo que el ViewModel |
| **Dispatchers.IO** | Hilos optimizados para red y disco |
| **LaunchedEffect** | Lanza una corrutina al entrar/cambiar una key |
| **rememberCameraPositionState** | Guarda zoom/posición del mapa entre recomposiciones |
| **BackStack** | Pila de pantallas visitadas (para navegar atrás) |
| **NavKey / NavDisplay** | API de Navigation 3 para definir rutas y mostrar pantallas |
| **FileProvider** | API de Android para compartir ficheros internos de forma segura |
| **Postgrest** | Módulo de Supabase para CRUD sobre PostgreSQL |
| **Result<T>** | Tipo de Kotlin que encapsula éxito o fallo |
| **sealed class** | Clase con un número cerrado de subclases conocidas |
| **data class** | Clase de Kotlin que solo guarda datos (auto-genera equals, copy, etc.) |

---

¡Suerte! Si te preguntan algo muy concreto y no lo encuentras aquí, ábrelo en el móvil en claude.ai, pega el fichero `.kt` correspondiente y pregúntale directamente.
