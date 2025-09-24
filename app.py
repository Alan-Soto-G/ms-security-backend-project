from __future__ import print_function
from flask import Flask, request, jsonify
import base64
import os
import pickle
from email.mime.text import MIMEText

from google.auth.transport.requests import Request
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build

# Configuración Flask
app = Flask(__name__)

# Alcances de Gmail API
SCOPES = ['https://www.googleapis.com/auth/gmail.send']

# Autenticación con Gmail API
def authenticate_gmail():
    creds = None
    if os.path.exists('confidencial/token.pickle'):
        with open('confidencial/token.pickle', 'rb') as token:
            creds = pickle.load(token)

    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                'confidencial/credentials.json', SCOPES)
            creds = flow.run_local_server(port=0)
        with open('confidencial/token.pickle', 'wb') as token:
            pickle.dump(creds, token)
    return creds

# Crear mensaje MIME codificado en base64
def create_message(sender, to, subject, message_text):
    message = MIMEText(message_text)
    message['to'] = to
    message['from'] = sender
    message['subject'] = subject
    raw_message = base64.urlsafe_b64encode(message.as_bytes())
    return {'raw': raw_message.decode()}

# Enviar el mensaje usando Gmail API
def send_message(service, user_id, message):
    try:
        sent_message = service.users().messages().send(userId=user_id, body=message).execute()
        print(f"✅ Mensaje enviado. ID: {sent_message['id']}")
        return sent_message
    except Exception as e:
        print(f"❌ Error: {e}")
        return None

# Ruta POST para enviar el correo desde Postman
@app.route('/enviarCorreo', methods=['POST'])
def enviar_correo():
    data = request.get_json()

    required_fields = ['to', 'subject', 'mensaje']
    if not all(field in data for field in required_fields):
        return jsonify({"error": f"Faltan campos. Se requieren: {required_fields}"}), 400

    sender = "a.b@autonoma.edu.co"  # Puedes dejarlo fijo o parametrizarlo
    to = data['to']
    subject = data['subject']
    mensaje_texto = data['mensaje']

    creds = authenticate_gmail()
    service = build('gmail', 'v1', credentials=creds)

    mensaje = create_message(sender, to, subject, mensaje_texto)
    enviado = send_message(service, 'me', mensaje)

    if enviado:
        return jsonify({"status": "ok", "id": enviado['id']}), 200
    else:
        return jsonify({"status": "error", "mensaje": "No se pudo enviar el correo"}), 500

if __name__ == '__main__':
    app.run(debug=True)
