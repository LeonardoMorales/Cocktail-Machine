#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include "FirebaseESP32.h"
#include <FastLED.h>
#include <ArduinoJson.h>

/* ----------------------------------
        WIFI CREDENTIALS
-------------------------------------*/
#define WIFI_SSID "IZZI-1B75"
#define WIFI_PASSWORD "D4AB82FC1B75"

// Retry Wifi Connection Vars
int count = 0;
int count2 = 0;

/* ----------------------------------
        FIREBASE
------------------------------------*/
#define FIREBASE_HOST "chat-app-8812e.firebaseio.com" //Do not include https:// in FIREBASE_HOST
#define FIREBASE_AUTH "6Os7dMCP9glr5o7kh1prYIC7RlBuk6FcFVQL1Afe"

FirebaseData firebaseDataRecive;
FirebaseData firebaseDataSend;

String parentPath = "/Machines/EDhmj2wGMXYpKQkdzlVn";
String childPath[3] = {"/currentProcess", "/isOnline", "/isWorking"};
size_t childPathSize = 3;

/* ----------------------------------
        STEPPER MOTOR 1 (HORIZONTAL)
------------------------------------*/
#define STEP_1 12
#define DIR_1 13
#define SLEEP_1 14

/* ----------------------------------
        STEPPER MOTOR 2 (VERTICAL)
------------------------------------*/
#define STEP_2 25
#define DIR_2 26
#define SLEEP_2 27

/* ----------------------------------
        FUNCTIONS
------------------------------------*/
void moveMachine(FirebaseData &data);
void desplazarBanda(int pisoNuevo);
void subirPlataforma(int cantidad);
void sendInitialMachineStateToFirebase();
void sendFinalMachineStateToFirebase();
void updateOnlineStatus();

// Valores actuales
int pisoActual = 1;
int alturaActual = 0;

// Valores nuevos
int pisoNuevo;
int alturaNueva;

/* ----------------------------------
        SECOND TASK
------------------------------------*/

TaskHandle_t Task1;

// How many leds in your strip?
#define NUM_LEDS 100

#define DATA_PIN 33

// Define the array of leds
CRGB leds[NUM_LEDS];

void fadeall()
{
  for (int i = 0; i < NUM_LEDS; i++)
  {
    leds[i].nscale8(250);
  }
}

void showLights(void *parameter)
{
  for (;;)
  {
    static uint8_t hue = 0;
    Serial.print("x");
    // First slide the led in one direction
    for (int i = 0; i < NUM_LEDS; i++)
    {
      // Set the i'th led to red
      leds[i] = CHSV(hue++, 255, 255);
      // Show the leds
      FastLED.show();
      // now that we've shown the leds, reset the i'th led to black
      // leds[i] = CRGB::Black;
      fadeall();
      // Wait a little bit before we loop around and do it again
      delay(10);
    }
    Serial.print("x");

    // Now go in the other direction.
    for (int i = (NUM_LEDS)-1; i >= 0; i--)
    {
      // Set the i'th led to red
      leds[i] = CHSV(hue++, 255, 255);
      // Show the leds
      FastLED.show();
      // now that we've shown the leds, reset the i'th led to black
      // leds[i] = CRGB::Black;
      fadeall();
      // Wait a little bit before we loop around and do it again
      delay(10);
    }
  }
  vTaskDelay(10);
}

void streamCallback(MultiPathStreamData stream)
{
  Serial.println();
  Serial.println("Stream Data1 available...");

  size_t numChild = sizeof(childPath) / sizeof(childPath[0]);

  for (size_t i = 0; i < numChild; i++)
  {
    if (stream.get(childPath[i]))
    {
      Serial.println("path: " + stream.dataPath + ", type: " + stream.type + ", value: " + stream.value);
      if (stream.dataPath == "/isOnline")
      {
        Serial.println("1");
        if (firebaseDataRecive.dataType() == "int")
        {
          Serial.println("2");
          if (firebaseDataRecive.intData() == 0)
          {
            // Actualizar campo isOnline a true en Firebase para comprobar que si esté conectado a internet
            Serial.println("3");
            Serial.println("UPDATING ONLINE STATUS...");
            updateOnlineStatus();
          }
        }
      }
      if (stream.dataPath == "/isWorking")
      {
        Serial.println("4");
        if (firebaseDataRecive.dataType() == "int")
        {
          Serial.println("5");
          if (firebaseDataRecive.intData() == 1)
          {
            Serial.println("6");
            //Obtener arreglo de currenProcess en Firestore y recorrerlo para mover maquina
            if (Firebase.get(firebaseDataRecive, parentPath + "/currentProcess"))
            {
              Serial.println("7");
              moveMachine(firebaseDataRecive);
            }
            else
            {
              Serial.println("FAILED");
              Serial.println("REASON: " + firebaseDataRecive.errorReason());
              Serial.println("------------------------------------");
              Serial.println();
            }
          }
        }
      }
    }
  }
  Serial.println();
}

void streamTimeoutCallback(bool timeout)
{
  if (timeout)
  {
    Serial.println();
    Serial.println("Stream timeout, resume streaming...");
    Serial.println();
  }
}

void setup()
{
  xTaskCreatePinnedToCore(
      showLights,
      "Task_showLights",
      1000,
      NULL,
      1,
      &Task1,
      0);

  LEDS.addLeds<WS2811, DATA_PIN, RGB>(leds, NUM_LEDS);
  LEDS.setBrightness(84);

  Serial.begin(115200); // inicializamos el puerto serie a 26600 baudios

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(1000);
    if (count == 8)
    {
      WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
      count = 0;
      Serial.println();
      Serial.print("Retrying connection...");
      delay(5000);
    }
    /*if (count2 == 12)
    {
      connection_status = 0;
      return 0;
    }*/
    count++;
    count2++;
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  //Set database read timeout to 1 minute (max 15 minutes)
  Firebase.setReadTimeout(firebaseDataRecive, 1000 * 60);

  //Set maximum Firebase read/store retry operation (0 - 255) in case of network problems and buffer overflow
  Firebase.setMaxRetry(firebaseDataRecive, 3);

  //Set the maximum Firebase Error Queues in collection (0 - 255).
  //Firebase read/store operation causes by network problems and buffer overflow will be added to Firebase Error Queues collection.
  Firebase.setMaxErrorQueue(firebaseDataRecive, 10);

  if (!Firebase.beginMultiPathStream(firebaseDataRecive, parentPath, childPath, childPathSize))
  {
    Serial.println("------------------------------------");
    Serial.println("Can't begin stream connection...");
    Serial.println("REASON: " + firebaseDataRecive.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }

  Firebase.setMultiPathStreamCallback(firebaseDataRecive, streamCallback, streamTimeoutCallback);

  updateOnlineStatus();

  pinMode(STEP_1, OUTPUT);
  pinMode(DIR_1, OUTPUT);
  pinMode(SLEEP_1, OUTPUT);

  pinMode(STEP_2, OUTPUT);
  pinMode(DIR_2, OUTPUT);
  pinMode(SLEEP_2, OUTPUT);
}

void loop() {}

void moveMachine(FirebaseData &data)
{
  if (data.dataType() == "json")
  {
    sendInitialMachineStateToFirebase();

    Serial.println();
    FirebaseJson *json = data.jsonObjectPtr();

    Serial.println("Iterate JSON data:");
    Serial.println();
    size_t len = json->iteratorBegin();
    String key, value = "";
    int type = 0;
    for (size_t i = 1; i < len; i++) // Se le resta la primera posicion y la ultima para solo procesar el arreglo de currentProcess
    {
      json->iteratorGet(i, type, key, value);
      Serial.print(i);
      Serial.print(", Value: ");
      Serial.println(value);
      if (key == "a_posicion")
      {
        pisoNuevo = value.toInt() - pisoActual;
        desplazarBanda(pisoNuevo);
        pisoActual = value.toInt();
      }
      else if (key == "cantidad")
      {
        // SUBIR PLATAFORMA PARA PRESIONAR DISPENSADOR
        Serial.print("La plataforma subira: ");
        Serial.print(value);
        Serial.println(" veces");
        subirPlataforma(value.toInt());
      }
    }
    json->iteratorEnd();
  }

  // REGRESAR BANDA A POSICIÓN 1
  pisoNuevo = -(pisoActual - 1);
  desplazarBanda(pisoNuevo);
  pisoActual = 1;

  // ACTUALIZAR ESTADO DE ISWORKING E
  sendFinalMachineStateToFirebase();
}

void desplazarBanda(int pisoNuevo)
{
  int piso;
  int distancia = 10; // CANTIDAD DE CENTIMETROS QUE SE QUIERE DESPLAZAR LA PLATAFORMA (CAMBIAR DE ACUERDO A DISEÑO DE MÁQUINA)

  digitalWrite(SLEEP_1, HIGH);

  if (pisoNuevo < 0)
  {
    Serial.print("Se desplaza hacia la izquierda ");
    piso = abs(pisoNuevo);
    Serial.print(piso);
    Serial.println(" pisos");
    digitalWrite(DIR_1, LOW); // AVANCE HACIA IZQUIERDA
    for (int i = 0; i <= ((piso * 2)* 250); i++)
    {
      digitalWrite(STEP_1, HIGH);
      delay(3);
      digitalWrite(STEP_1, LOW);
      delay(3);
      /*Serial.print("GIRO A FAVOR DE LAS MANECILLAS ");
      Serial.print(i);
      Serial.println(" PASOS");*/
    }
  }
  else if (pisoNuevo > 0)
  {
    Serial.print("Se desplaza hacia la derecha ");
    piso = abs(pisoNuevo);
    Serial.print(piso);
    Serial.println(" pisos");
    digitalWrite(DIR_1, HIGH); // AVANCE HACIA DERECHA
    for (int i = 0; i <= ((piso * distancia) * 50); i++)
    {
      digitalWrite(STEP_1, HIGH);
      delay(3);
      digitalWrite(STEP_1, LOW);
      delay(3);
      /*Serial.print("GIRO A FAVOR DE LAS MANECILLAS ");
      Serial.print(i);
      Serial.println(" PASOS");*/
    }
  }
  else
  {
    Serial.println("Se desplaza al piso 1");
  }

  digitalWrite(SLEEP_1, LOW);
}

void subirPlataforma(int cantidad)
{
  int altura = 4; // CANTIDAD DE CENTIMETROS QUE SE QUIERE SUBIR LA PLATAFORMA (CAMBIAR DE ACUERDO A DISEÑO DE MÁQUINA)

  digitalWrite(SLEEP_2, HIGH);

  for (int i = 0; i < cantidad; i++)
  {
    /*Serial.print("Sube ");
    Serial.print(altura);
    Serial.println(" centimetros");*/
    digitalWrite(DIR_2, LOW); // AVANCE HACIA ARRIBA
    for (int i = 0; i <= (altura * 250); i++)
    {
      digitalWrite(STEP_2, HIGH);
      delay(4);
      digitalWrite(STEP_2, LOW);
      delay(4);
    }

    digitalWrite(SLEEP_2, LOW);
    delay(2500); //Un delay de 3 segundos para permitir que el liquido salga por completo del recipiente
    digitalWrite(SLEEP_2, HIGH);

    /*Serial.print("Baja ");
    Serial.print(altura);
    Serial.println(" centimetros");*/
    digitalWrite(DIR_2, HIGH); // AVANCE HACIA ABAJO
    for (int i = 0; i <= (altura * 250); i++)
    {
      digitalWrite(STEP_2, HIGH);
      delay(4);
      digitalWrite(STEP_2, LOW);
      delay(4);
    }
    digitalWrite(SLEEP_2, LOW);
    delay(500); //Un delay de 1 segundos para permitir que el recipiente se vuelva a llenar
    digitalWrite(SLEEP_2, HIGH);
  }

  digitalWrite(SLEEP_2, LOW);
}

void sendInitialMachineStateToFirebase()
{
  if (WiFi.status() == WL_CONNECTED)
  {
    HTTPClient http;
    http.begin("https://chat-app-8812e.firebaseapp.com/api/v1/isWorking/EDhmj2wGMXYpKQkdzlVn");
    http.addHeader("Content-Type", "application/json");

    StaticJsonDocument<20> tempDocument;
    tempDocument["isWorking"] = true;
    char buffer[20];
    serializeJson(tempDocument, buffer);
    http.PATCH(buffer);
    http.end();
  }
  else
  {
    Serial.println("Check your Wifi connection!");
  }
}

void sendFinalMachineStateToFirebase()
{
  Serial.println("Bebida Finalizada!!!");
  if (WiFi.status() == WL_CONNECTED)
  {
    HTTPClient http;
    http.begin("https://chat-app-8812e.firebaseapp.com/api/v1/isWorking/EDhmj2wGMXYpKQkdzlVn");
    http.addHeader("Content-Type", "application/json");

    StaticJsonDocument<20> tempDocument;
    tempDocument["isWorking"] = false;
    char buffer[20];
    serializeJson(tempDocument, buffer);
    http.PATCH(buffer);
    http.end();
  }
  else
  {
    Serial.println("Check your Wifi connection!");
  }

  // ESTABLECER isWorking = false
  FirebaseJson jsonIsWorking;
  jsonIsWorking.set("isWorking", 0);
  if (Firebase.updateNode(firebaseDataSend, parentPath, jsonIsWorking))
  {
    Serial.println("isWorking = false");
  }
  else
  {
    Serial.println(firebaseDataSend.errorReason());
  }
}

void updateOnlineStatus()
{
  FirebaseJson json;
  json.set("isOnline", 1);

  if (Firebase.updateNode(firebaseDataSend, parentPath, json))
  {

    Serial.println("UPDATE ONLINE STATUS PASSED");
    Serial.println();
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + firebaseDataSend.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
}
