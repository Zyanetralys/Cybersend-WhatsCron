<h1>CyberSend — Programador de Mensajes de WhatsApp</h1>

<pre class="ascii"> ██████╗██╗   ██╗██████╗ ███████╗██████╗ ███████╗███████╗███╗   ██╗██████╗
██╔════╝╚██╗ ██╔╝██╔══██╗██╔════╝██╔══██╗██╔════╝██╔════╝████╗  ██║██╔══██╗
██║      ╚████╔╝ ██████╔╝█████╗  ██████╔╝███████╗█████╗  ██╔██╗ ██║██║  ██║
██║       ╚██╔╝  ██╔══██╗██╔══╝  ██╔══██╗╚════██║██╔══╝  ██║╚██╗██║██║  ██║
╚██████╗   ██║   ██████╔╝███████╗██║  ██║███████║███████╗██║ ╚████║██████╔╝
 ╚═════╝   ╚═╝   ╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═══╝╚═════╝</pre>

<div class="center">
    <span class="badge"><img src="https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java"></span>
    <span class="badge"><img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot"></span>
    <span class="badge"><img src="https://img.shields.io/badge/license-MIT-00ff41?style=flat-square" alt="License"></span>
    <span class="badge"><img src="https://img.shields.io/badge/status-active-00ff41?style=flat-square" alt="Status"></span>
    <span class="badge"><img src="https://img.shields.io/badge/GitHub_Pages-ready-181717?style=flat-square&logo=github" alt="GitHub Pages"></span>

    <p><strong>Programa mensajes de WhatsApp con precisión. Sin tonterías, sin suscripciones.</strong></p>

    <p>
        <a href="#demo">Demo</a> · 
        <a href="#inicio-rapido">Inicio Rápido</a> · 
        <a href="#arquitectura">Arquitectura</a> · 
        <a href="#referencia-api">Referencia API</a> · 
        <a href="#contribuir">Contribuir</a>
    </p>
</div>

<hr>

<h2 id="que-es-esto">¿Qué es esto?</h2>

<p>CyberSend es un programador de mensajes de WhatsApp autoalojado. Escribes un mensaje, eliges una hora, y se dispara — automáticamente, vía WhatsApp Web.</p>

<p>Se ejecuta en dos modos:</p>

<ul>
    <li><strong>HTML independiente</strong> — coloca <code>index.html</code> en cualquier lugar, incluyendo GitHub Pages. Sin servidor, sin dependencias. El navegador actúa como programador y abre WhatsApp Web en el momento adecuado usando enlaces profundos <code>wa.me</code>.</li>
    <li><strong>Backend completo (Spring Boot)</strong> — almacenamiento persistente, API REST, actualizaciones WebSocket en tiempo real, y un despachador basado en cron adecuado. Se conecta a WhatsApp vía puente de sesión o la API Business oficial.</li>
</ul>

<p>La interfaz es intencionalmente exagerada: lluvia Matrix, líneas de exploración CRT, tipografía con efecto glitch. Porque ¿por qué no?</p>

<hr>

<h2 id="demo">Demo</h2>

<p>Abre <code>index.html</code> directamente en tu navegador. Haz clic en <strong>SIMULAR CONEXIÓN (DEMO)</strong>, programa un mensaje, y observa el registro de la terminal. Sin instalación requerida.</p>

<p>Para GitHub Pages: sube <code>index.html</code> a la raíz de tu repositorio, habilita Pages desde la configuración del repositorio, listo.</p>

<hr>

<h2 id="inicio-rapido">Inicio Rápido</h2>

<h3>Independiente (solo navegador)</h3>

<p>Sin configuración. Solo abre el archivo:</p>

<pre><code>open index.html
# o
python3 -m http.server 3000 &amp;&amp; open http://localhost:3000</code></pre>

<p>Para desplegar en GitHub Pages:</p>

<pre><code>git init
git add index.html
git commit -m "init: deploy CyberSend"
git remote add origin https://github.com/youruser/cybersend.git
git push -u origin main
# Luego: Settings → Pages → Deploy from branch → main → / (root)</code></pre>

<p>Tu aplicación estará en vivo en <code>https://youruser.github.io/cybersend</code>.</p>

<h3>Backend (Spring Boot)</h3>

<p><strong>Requisitos:</strong> Java 17+, Maven 3.8+</p>

<pre><code>git clone https://github.com/youruser/cybersend.git
cd cybersend
mvn spring-boot:run</code></pre>

<p>El servidor inicia en <code>http://localhost:8080</code>. Abre <code>index.html</code>, establece el campo <strong>Punto de Acceso API</strong> en <code>http://localhost:8080</code>, y haz clic en <strong>PROBAR CONEXIÓN</strong>. El frontend cambiará a modo backend automáticamente.</p>

<p>Para construir un JAR completo para despliegue:</p>

<pre><code>mvn clean package -DskipTests
java -jar target/whatsapp-scheduler-1.0.0.jar</code></pre>

<hr>

<h2 id="arquitectura">Arquitectura</h2>

<pre class="ascii">┌─────────────────────────────────────────────────────────┐
│                    index.html (Navegador)                │
│                                                          │
│  ┌────────────────┐      ┌───────────────────────────┐  │
│  │  Modo Local    │      │      Modo Backend         │  │
│  │  (por defecto) │      │                           │  │
│  │                │      │  REST  ──▶  /api/*        │  │
│  │  setInterval   │      │  WS    ──▶  /ws (STOMP)   │  │
│  │  (checks 15s)  │      │                           │  │
│  │       │        │      │                           │  │
│  │  Enlaces wa.me │      │                           │  │
│  └────────────────┘      └───────────────────────────┘  │
└──────────────────────────────────┬──────────────────────┘
                                   │ HTTP / WebSocket
                    ┌──────────────▼──────────────────┐
                    │       Spring Boot 3.2            │
                    │                                  │
                    │  ApiController   (REST)          │
                    │  WebSocketConfig (broker STOMP)  │
                    │  MessageSchedulerService (@Scheduled 30s) │
                    │  WhatsAppService (gestión sesión)│
                    │                                  │
                    │  H2 (dev) / PostgreSQL (prod)    │
                    └──────────────┬───────────────────┘
                                   │
                    ┌──────────────▼───────────────────┐
                    │      Integración WhatsApp        │
                    │                                  │
                    │  Opción A: enlaces profundos wa.me│
                    │  Opción B: puente whatsapp-web.js│
                    │  Opción C: Business API (Meta)   │
                    └──────────────────────────────────┘</pre>

<p>El backend persiste los mensajes en H2 (basado en archivo, sobrevive a reinicios). Una tarea <code>@Scheduled</code> se ejecuta cada 30 segundos, recoge cualquier mensaje <code>PENDING</code> cuyo <code>scheduled_at &lt;= NOW()</code>, y los despacha. Las transiciones de estado se envían a los clientes conectados vía STOMP WebSocket en <code>/topic/message-updates</code>.</p>

<hr>

<h2 id="integracion-whatsapp">Integración con WhatsApp</h2>

<p>CyberSend admite tres estrategias de integración, ordenadas por complejidad:</p>

<h3>Enlaces profundos wa.me (sin configuración)</h3>

<p>La opción de respaldo por defecto. Genera una URL <code>https://wa.me/{phone}?text={encoded}</code> y la abre en una nueva pestaña. Funciona en escritorio y móvil. El usuario aún necesita presionar Enviar manualmente — pero combinado con el programador, abre WhatsApp Web en el momento exacto.</p>

<p>Sin credenciales, sin claves API, sin gestión de sesión.</p>

<h3>Puente whatsapp-web.js (recomendado para automatización completa)</h3>

<p>Ejecuta un pequeño acompañante Node.js que envuelve <a href="https://github.com/pedroslopez/whatsapp-web.js">whatsapp-web.js</a> y expone un endpoint REST local. El servicio Java llama a ese endpoint para enviar mensajes sin ninguna interacción del usuario.</p>

<pre><code>cd bridge
npm install
node server.js
# Escanea el código QR una vez — la sesión se persiste en .wwebjs_auth/</code></pre>

<p>El puente expone <code>POST /send</code> con <code>{ phone, message }</code>. Edita <code>WhatsAppService.java</code> para apuntar a él:</p>

<pre><code class="language-java">// WhatsAppService.java — callNodeBridge()
String url = "http://localhost:3001/send";
RestTemplate rt = new RestTemplate();
rt.postForEntity(url, Map.of("phone", phoneNumber, "message", message), String.class);</code></pre>

<h3>API Business de WhatsApp (producción / alto volumen)</h3>

<p>Requiere una cuenta Meta Business verificada y un número de teléfono registrado. Establece tu token como variable de entorno y descomenta el bloque relevante en <code>WhatsAppService.java</code>:</p>

<pre><code>export WHATSAPP_BUSINESS_TOKEN=your_token_here
export WHATSAPP_PHONE_NUMBER_ID=your_number_id</code></pre>

<pre><code class="language-java">// WhatsAppService.java
String url = "https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages";
// ... payload estándar de Business API</code></pre>

<p>Se aplican límites de tasa y precios — consulta <a href="https://developers.facebook.com/docs/whatsapp/cloud-api/get-started">la documentación de Meta</a>.</p>

<hr>

<h2 id="estructura-proyecto">Estructura del Proyecto</h2>

<pre class="ascii">cybersend/
│
├── index.html                              # Frontend independiente — despliega esto en GitHub Pages
├── pom.xml
├── README.md
│
└── src/
    ├── main/
    │   ├── java/com/whatsapp/scheduler/
    │   │   │
    │   │   ├── WhatsAppSchedulerApplication.java   # Punto de entrada, @EnableScheduling
    │   │   │
    │   │   ├── controller/
    │   │   │   └── ApiController.java              # Todos los endpoints REST, CORS configurado
    │   │   │
    │   │   ├── service/
    │   │   │   ├── WhatsAppService.java            # Ciclo de vida sesión, QR, lógica envío
    │   │   │   └── MessageSchedulerService.java    # Despachador @Scheduled, recurrencia
    │   │   │
    │   │   ├── model/
    │   │   │   ├── ScheduledMessage.java           # Entidad JPA (estado, recurrencia, zona horaria)
    │   │   │   ├── WhatsAppSession.java            # Entidad sesión (QR, estado, teléfono)
    │   │   │   ├── ScheduledMessageRepository.java # @Query personalizado para mensajes pendientes
    │   │   │   └── WhatsAppSessionRepository.java
    │   │   │
    │   │   └── config/
    │   │       └── WebSocketConfig.java            # Broker STOMP, endpoint /ws, SockJS
    │   │
    │   └── resources/
    │       └── application.properties             # H2, JPA, configuración servidor
    │
    └── test/
        └── java/                                  # (añade tus tests aquí)</code></pre>

<hr>

<h2 id="configuracion">Configuración</h2>

<p><code>src/main/resources/application.properties</code>:</p>

<pre><code class="language-properties"># Cambia a PostgreSQL para producción
spring.datasource.url=jdbc:postgresql://localhost:5432/cybersend
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update

# Ajusta el intervalo de despacho (ms)
# Por defecto: 30000 (30 segundos)
# Para programación de alta precisión, reduce a 5000</code></pre>

<p>Añade el controlador PostgreSQL a <code>pom.xml</code> al cambiar:</p>

<pre><code class="language-xml">&lt;dependency&gt;
    &lt;groupId&gt;org.postgresql&lt;/groupId&gt;
    &lt;artifactId&gt;postgresql&lt;/artifactId&gt;
    &lt;scope&gt;runtime&lt;/scope&gt;
&lt;/dependency&gt;</code></pre>

<hr>

<h2 id="referencia-api">Referencia API</h2>

<p>Todos los endpoints están bajo <code>/api</code>. CORS está abierto (<code>*</code>) por defecto — restríngelo para producción.</p>

<h3>Mensajes</h3>

<table>
    <thead>
        <tr><th>Método</th><th>Endpoint</th><th>Cuerpo</th><th>Descripción</th></tr>
    </thead>
    <tbody>
        <tr><td><code>GET</code></td><td><code>/api/messages</code></td><td>—</td><td>Devuelve todos los mensajes, ordenados por <code>scheduled_at DESC</code></td></tr>
        <tr><td><code>POST</code></td><td><code>/api/messages</code></td><td><code>ScheduledMessage JSON</code></td><td>Crea un mensaje programado</td></tr>
        <tr><td><code>POST</code></td><td><code>/api/messages/{id}/send-now</code></td><td>—</td><td>Anula la programación y despacha inmediatamente</td></tr>
        <tr><td><code>POST</code></td><td><code>/api/messages/{id}/cancel</code></td><td>—</td><td>Marca como <code>CANCELLED</code>, omitido por el despachador</td></tr>
        <tr><td><code>DELETE</code></td><td><code>/api/messages/{id}</code></td><td>—</td><td>Cancela y elimina</td></tr>
    </tbody>
</table>

<p><strong>POST /api/messages — cuerpo de la solicitud:</strong></p>

<pre><code class="language-json">{
  "phoneNumber": "34612345678",
  "contactName": "Ana García",
  "messageContent": "Hola, recuerda la reunión a las 10.",
  "scheduledAt": "2025-06-15T09:55:00",
  "recurrence": "NONE",
  "timezone": "Europe/Madrid"
}</code></pre>

<p><code>recurrence</code> acepta: <code>NONE</code>, <code>DAILY</code>, <code>WEEKLY</code>, <code>MONTHLY</code>.</p>
<p><code>status</code> se gestiona del lado del servidor: <code>PENDING → SENDING → SENT | FAILED</code>.</p>

<h3>Sesión</h3>

<table>
    <thead>
        <tr><th>Método</th><th>Endpoint</th><th>Cuerpo</th><th>Descripción</th></tr>
    </thead>
    <tbody>
        <tr><td><code>GET</code></td><td><code>/api/session</code></td><td>—</td><td>Estado actual de la sesión</td></tr>
        <tr><td><code>POST</code></td><td><code>/api/session/connect</code></td><td>—</td><td>Genera QR, estado → <code>QR_PENDING</code></td></tr>
        <tr><td><code>POST</code></td><td><code>/api/session/confirm</code></td><td><code>{ "phoneNumber": "346..." }</code></td><td>Confirma escaneo, estado → <code>CONNECTED</code></td></tr>
        <tr><td><code>POST</code></td><td><code>/api/session/disconnect</code></td><td>—</td><td>Termina la sesión</td></tr>
    </tbody>
</table>

<h3>Utilidades</h3>

<table>
    <thead>
        <tr><th>Método</th><th>Endpoint</th><th>Descripción</th></tr>
    </thead>
    <tbody>
        <tr><td><code>GET</code></td><td><code>/api/stats</code></td><td>Conteos por estado (<code>total</code>, <code>pending</code>, <code>sent</code>, <code>failed</code>)</td></tr>
        <tr><td><code>GET</code></td><td><code>/api/health</code></td><td>Verificación de estado (<code>{ status: "ONLINE", time: "..." }</code>)</td></tr>
        <tr><td><code>GET</code></td><td><code>/api/server-time</code></td><td>Hora del servidor + zona horaria + zonas IANA disponibles</td></tr>
        <tr><td><code>GET</code></td><td><code>/api/wa-link?phone=346...&amp;message=Hola</code></td><td>Genera un enlace profundo <code>wa.me</code></td></tr>
    </tbody>
</table>

<h3>WebSocket</h3>

<p>Conéctate a <code>ws://localhost:8080/ws</code> (SockJS + STOMP).</p>
<p>Suscríbete a:</p>
<ul>
    <li><code>/topic/message-updates</code> — se activa en cada cambio de estado de mensaje</li>
    <li><code>/topic/session-status</code> — se activa en conexión/desconexión/QR de sesión</li>
</ul>

<hr>

<h2 id="ciclo-vida-mensaje">Ciclo de Vida del Mensaje</h2>

<pre class="ascii">PENDING ──── el despachador recoge ──▶ SENDING ──── éxito ──▶ SENT
                                         │
                                         └── fallo ──▶ FAILED
                                         └── sin sesión ──▶ PENDING (se emite enlace respaldo wa.me)

PENDING ──── usuario cancela ──▶ CANCELLED</pre>

<p>Mensajes recurrentes: cuando un mensaje <code>SENT</code> tiene <code>recurrence != NONE</code>, el despachador lo clona con <code>scheduled_at</code> incrementado por el intervalo de recurrencia. El clon comienza como <code>PENDING</code>.</p>

<hr>

<h2 id="atajos-teclado">Atajos de Teclado</h2>

<table>
    <thead>
        <tr><th>Atajo</th><th>Acción</th></tr>
    </thead>
    <tbody>
        <tr><td><code>Ctrl + Enter</code></td><td>Programar mensaje</td></tr>
        <tr><td><code>Ctrl + Shift + Enter</code></td><td>Enviar inmediatamente</td></tr>
        <tr><td><code>Escape</code></td><td>Cerrar cualquier modal abierto</td></tr>
    </tbody>
</table>

<hr>

<h2 id="caracteristicas">Características</h2>

<ul>
    <li>Programa mensajes a cualquier número de WhatsApp con fecha, hora y zona horaria exactas</li>
    <li>Recurrencia: diaria, semanal, mensual (auto-clona después del despacho)</li>
    <li>Actualizaciones de estado en tiempo real vía WebSocket — sin polling</li>
    <li>Geolocalización del navegador para autodetección de zona horaria</li>
    <li>Backend opcional: el archivo HTML es completamente autocontenido y funciona sin conexión</li>
    <li>Base de datos embebida H2 con persistencia en archivo (sin pérdida de datos al reiniciar)</li>
    <li>Enlaces de respaldo <code>wa.me</code> siempre disponibles independientemente del estado de la sesión</li>
    <li>Flujo de autenticación de WhatsApp Web basado en QR (simulado en modo demo)</li>
    <li>Soporte CORS completo para despliegues frontend/backend de origen cruzado</li>
    <li>Consola H2 disponible en <code>/h2-console</code> en modo desarrollo</li>
</ul>

<hr>

<h2 id="notas-automatizacion">Notas sobre Automatización de WhatsApp</h2>

<p>Los Términos de Servicio de WhatsApp prohíben la automatización no autorizada de cuentas personales. El enfoque de enlace profundo <code>wa.me</code> es totalmente conforme — solo pre-rellena el mensaje y requiere que el usuario presione Enviar. La automatización completa vía <code>whatsapp-web.js</code> opera en un área gris para uso personal; para uso comercial/de producción, la API Business oficial es el camino correcto.</p>

<p>Este proyecto está destinado a productividad personal y herramientas para desarrolladores. Úsalo responsablemente.</p>

<hr>

<div class="center">
    <sub>Construido con Spring Boot 3.2 · Java 17 · Vanilla JS · Sin instalación npm requerida para el frontend</sub>
</div>

</body>
</html>
