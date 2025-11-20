
1. Descarga de Repositorios
A continuación, se listan los repositorios necesarios para el funcionamiento del sistema, incluyendo backend, microservicio de Python, cliente Node.js, scripts de base de datos y manifiestos de Kubernetes.
1.1. Repositorios
Módulo	Repositorio
Microservicio Python (Impuestos)	https://github.com/JefersonCaceres/ms-billing-tax-python.git

Scripts BD PostgreSQL	https://github.com/JefersonCaceres/billing-db-scripts.git

Cliente Node.js	https://github.com/JefersonCaceres/billing-node-client.git

Backend Java (Core)	https://github.com/JefersonCaceres/ms-billing-backend-java.git

Manifiestos de Kubernetes	https://github.com/JefersonCaceres/billing-k8s-manifests.git

1.2. Clonar los repositorios
git clone https://github.com/JefersonCaceres/ms-billing-tax-python.git
git clone https://github.com/JefersonCaceres/billing-db-scripts.git
git clone https://github.com/JefersonCaceres/billing-node-client.git
git clone https://github.com/JefersonCaceres/ms-billing-backend-java.git
git clone https://github.com/JefersonCaceres/billing-k8s-manifests.git
________________________________________
2. Generación de Imágenes Docker
Cada proyecto tiene su propio Dockerfile configurado.
Ejecutar los siguientes comandos para generar las imágenes:
________________________________________
2.1. Backend Java
Ubicarse en la carpeta:
ms-billing-backend-java/
Construir JAR:
mvn clean package -DskipTests
Construir la imagen Docker:
docker build -t billing-backend-java:1.0.0 .
________________________________________
2.2. Microservicio Python
Ubicarse en:
ms-billing-tax-python/
Construcción:
docker build -t billing-python:1.0.0 .
________________________________________
2.3. Cliente Node.js
Ubicarse en:
billing-node-client/
Construcción:
docker build -t billing-node-client:1.0.0 .
________________________________________
2.4. Manifiestos de Kubernetes
Los manifiestos no generan imagen; simplemente despliegan los servicios usando las imágenes previamente construidas.
________________________________________
3. Despliegue en Kubernetes
Asegúrese de estar ubicado en la ruta:
billing-k8s-manifests/
________________________________________
3.1. PostgreSQL
kubectl apply -f postgres-pvc.yaml
kubectl apply -f postgres-deployment.yaml
kubectl apply -f postgres-service.yaml
________________________________________
3.2. Microservicio Python
kubectl apply -f python-deployment.yaml
kubectl apply -f python-service.yaml
________________________________________
3.3. Backend Java
kubectl apply -f backend-deployment.yaml
kubectl apply -f backend-service.yaml
________________________________________
3.4. Cliente Node.js
kubectl apply -f node-deployment.yaml
kubectl apply -f node-service.yaml
________________________________________
3.5. Validaciones
Ver pods:
kubectl get pods
Ver servicios:
kubectl get svc
Ver logs:
kubectl logs -f deployment/billing-backend
kubectl logs -f deployment/python-deployment
kubectl logs -f deployment/node-deployment
________________________________________
4. Consumo de Endpoints desde el Backend Java
El backend expone un conjunto de endpoints para administrar clientes y generar facturas.
Controlador:
@RestController
@RequestMapping("/api/clients")
public class ClientController {
________________________________________
4.1. Crear Cliente
POST /api/clients/create
Body:
{
  "documentType": "CC",
  "document": "12345",
  "name": "Jefferson",
  "email": "jeff@mail.com",
  "phone": "3000000000",
  "address": "Barranquilla",
  "active": true
}
________________________________________
4.2. Consultar Cliente por Documento
GET /api/clients/get/{document}
Ejemplo:
GET /api/clients/get/12345
________________________________________
4.3. Actualizar Cliente
PUT /api/clients/update/{document}
Body igual al de creación.
________________________________________
4.4. Eliminar Cliente (Soft Delete)
DELETE /api/clients/delete/{document}
Realiza un delete lógico y replica la desactivación a Oracle.
________________________________________
4.5. Generar Factura
POST /api/clients/bill/{document}
Ejemplo:
Body:
{
  "items": [
    { "description": "Prod 1", "quantity": 2, "unit_price": 100000 },
    { "description": "Prod 2", "quantity": 1, "unit_price": 150000 }
  ]
}
El backend:
1.	Valida cliente
2.	Obtiene parámetros TAX + DISCOUNT desde PostgreSQL
3.	Construye la solicitud final
4.	Llama al microservicio Python
5.	Recibe subtotal, impuestos y descuentos
6.	Registra log
7.	Crea factura en Oracle
8.	Retorna la respuesta al frontend o Node.js
________________________________________
5. Explicación del Cliente Node.js
El cliente Node.js funciona como un script automatizado para probar todo el flujo del backend.
5.1. ¿Qué hace el script?
1.	Crea un cliente → usando el endpoint Java.
2.	Consulta ese cliente → verificando lectura correcta.
3.	Genera una factura → Java llama a Python y luego Oracle.
4.	Elimina el cliente → realiza soft delete.
5.	Imprime resultados en consola.
Flujo simplificado:
await axios.post(`${BASE_URL}/api/clients/create`)
await axios.get(`${BASE_URL}/api/clients/get/${document}`)
await axios.post(`${BASE_URL}/api/clients/bill/${document}`)
await axios.delete(`${BASE_URL}/api/clients/delete/${document}`)
5.2. ¿Por qué se despliega en Kubernetes?
Porque dentro del cluster:
•	El backend Java necesita comunicarse con Python usando python-service:8000
•	El Node.js necesita comunicarse con Java usando billing-backend-service:8080
•	Node.js sirve como tester automático del entorno Kubernetes
Este script te garantiza que tu entorno está funcionando correctamente sin necesidad de usar Postman.

