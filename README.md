# 📸 MiGalería GPS

Aplicación nativa para Android desarrollada en **Kotlin** que permite registrar usuarios, iniciar sesión, capturar fotografías con la cámara del dispositivo, almacenar comentarios, guardar la ubicación GPS y visualizar las fotos capturadas en una lista interactiva con soporte para tema claro/oscuro y gestos de eliminación.

---

## 🌟 Características Principales

1. **Autenticación Local de Usuarios:**
   - Registro con nombre, apellidos, correo electrónico y contraseña.
   - Inicio de sesión validando credenciales guardadas en la base de datos local **SQLite**.
   - Opción para ocultar y mostrar contraseña con el icono del ojo (`password_toggle`).
   - Encriptación/hashing de contraseñas mediante SHA-256.

2. **Captura de Fotografía y Ubicación GPS:**
   - Integración con la cámara nativa de Android mediante `ActivityResultContracts.TakePicture()`.
   - Almacenamiento de imágenes en la carpeta privada de la aplicación (`Pictures`).
   - Obtención de coordenadas GPS (Latitud y Longitud) mediante `LocationManager`.
   - Registro de comentarios personalizados por fotografía.

3. **Galería Interactiva:**
   - Consulta de fotografías en `RecyclerView` filtradas por el usuario con sesión activa.
   - **Gesto Deslizar (Swipe-to-Delete):** Desplaza horizontalmente cualquier elemento para revelar la opción de eliminar la foto del almacenamiento local y de la base de datos.
   - Vista detallada mediante un cuadro de diálogo (`AlertDialog`) con la imagen completa, fecha, comentario y coordenadas GPS.

4. **Soporte para Tema Claro y Oscuro (Material Design 3):**
   - Conmutador (*Switch*) en la pantalla de inicio de sesión para cambiar entre **Modo Claro** y **Modo Oscuro** a nivel global en toda la aplicación.
   - Persistencia de la preferencia de tema usando `SharedPreferences`.

---

## 🛠️ Tecnologías y Herramientas Utilizadas

- **Lenguaje:** Kotlin
- **Plataforma:** Android SDK (minSdk: 26 / compileSdk: 37 / targetSdk: 35)
- **Base de Datos:** SQLite (`SQLiteOpenHelper`)
- **Interfaz de Usuario:** Material Design 3 (`MaterialSwitch`, `TextInputLayout`, `RecyclerView`, `ItemTouchHelper`)
- **Permisos y Cámara:** `FileProvider`, `ActivityResultContracts`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- **Gradle:** Android Gradle Plugin 9.3.0 / Kotlin DSL

---

## 📂 Estructura del Proyecto

```
MiGaleriaGPS/
└── app/src/main/
    ├── java/xyz/vmartinez/MiGaleriaGPS/
    │   ├── database/
    │   │   └── DatabaseHelper.kt       # Gestión de tablas usuarios y fotos en SQLite
    │   ├── LoginActivity.kt            # Inicio de sesión y cambio de tema claro/oscuro
    │   ├── RegistroActivity.kt         # Registro de nuevos usuarios
    │   ├── MainActivity.kt             # Captura de foto, ubicación GPS y comentario
    │   ├── ListaDeFotosActivity.kt     # Galería con RecyclerView y gesto de deslizar
    │   └── Security.kt                 # Hash SHA-256 para contraseñas
    └── res/
        ├── layout/                      # Diseños XML adaptables (Login, Registro, Main, Lista, Item)
        ├── drawable/                    # Icono vectorial adaptativo (GPS Pin con Cámara)
        └── values/ e values-night/      # Estilos y paletas de colores Día/Noche
```

---

## 🚀 Requisitos e Instalación

1. **Clonar o descargar el repositorio:**
   ```bash
   git clone https://github.com/TU_USUARIO/MiGaleriaGPS.git
   ```
2. **Abrir en Android Studio:**
   - Abre Android Studio.
   - Selecciona **Open** y elige la carpeta del proyecto `MiGaleriaGPS`.
3. **Ejecutar la aplicación:**
   - Conecta un dispositivo físico Android (Android 8.0 API 26 o superior) o usa un emulador con cámara y ubicación configuradas.
   - Presiona **Run** (`Shift + F10`).

---

## 📄 Licencia

Proyecto didáctico desarrollado para fines académicos y de evaluación.
