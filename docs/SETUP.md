# EventFlow Setup Guide

This guide provides instructions on how to set up and run the EventFlow full-stack application locally.

## Prerequisites

Ensure you have the following installed on your machine:
- **Java 21**
- **Node.js 20+** (and `npm`)
- **Docker** and **Docker Compose**
- **Razorpay Account** (for test mode credentials)
- **Brevo Account** (for SMTP credentials)

## 1. Clone the Repository

```bash
git clone https://github.com/Ranjan-3105/EventFlow.git
cd EventFlow
```

## 2. Environment Configuration

You need to set up the environment variables for both the backend and frontend. Templates have been provided in `.env.example` files.

### Backend

Copy the backend example file:
```bash
cd Backend
cp .env.example .env
```

Open `Backend/.env` and fill in the required values:
- `JWT_SECRET`: A secure, random string (minimum 32 characters) for signing JWT tokens.
- `RAZORPAY_KEY_ID`: Your Razorpay Test Key ID (e.g., `rzp_test_...`).
- `RAZORPAY_KEY_SECRET`: Your Razorpay Test Key Secret.
- `BREVO_SMTP_LOGIN`: Your Brevo SMTP login email.
- `BREVO_SMTP_KEY`: Your Brevo master password / SMTP key.

### Frontend

Copy the frontend example file:
```bash
cd ../frontend
cp .env.example .env
```

Open `frontend/.env` and configure the API base URL:
- `VITE_API_BASE_URL=/api` (This works locally out-of-the-box because of the Vite proxy).

## 3. Start the Infrastructure Services

EventFlow requires PostgreSQL (Database), Redis (Caching/Locking), and Apache Kafka (Event streaming for notifications). We provide a Docker Compose configuration to spin these up effortlessly.

```bash
cd ../Backend
docker-compose up -d
```

> **Note:** This command starts PostgreSQL on port `5432`, Redis on `6379`, and Kafka on `9092`. Ensure these ports are not being used by other services on your machine.

## 4. Start the Backend (Spring Boot)

Once the infrastructure is running, start the Spring Boot backend using the included Maven wrapper.

From the `Backend` directory:

```bash
# On Windows
.\mvnw spring-boot:run

# On Mac/Linux
./mvnw spring-boot:run
```

The backend server will start on `http://localhost:8080`.

## 5. Start the Frontend (React + Vite)

The frontend is a modern React application built with Vite and Tailwind CSS. It uses a development proxy configured in `vite.config.ts` to forward `/api/*` requests to the Spring Boot backend (`http://localhost:8080`), bypassing CORS issues during local development.

Open a new terminal window and navigate to the frontend directory:

```bash
cd frontend
npm install
npm run dev
```

The frontend application will start on `http://localhost:5173`.

## 6. How to Perform a Test Booking

To verify that the entire system is working correctly:
1. Open the application at `http://localhost:5173`.
2. Click **Sign Up** to register a new user account.
3. Log in with your new credentials.
4. Browse the **Events** and click on an event to view its details.
5. Click **Select Seats**, choose one or more available seats, and click **Continue to Payment**.
6. The Razorpay checkout modal will appear. Since you configured Razorpay in test mode, you can use Razorpay's provided test cards (e.g., card number `4111 1111 1111 1111`) to simulate a successful payment.
7. Upon success, you will see the Booking Confirmation page, and a Kafka event will trigger an email notification via Brevo SMTP to your registered email address.

## Troubleshooting

- **Port Conflicts (5432, 6379, 9092):** If Docker fails to start a container, it usually means a local instance of PostgreSQL, Redis, or Kafka is already running on your machine. Stop your local services or change the port bindings in `docker-compose.yml`.
- **Database Connection Refused:** Wait a few seconds for the PostgreSQL container to initialize fully before starting the Spring Boot backend.
- **Payment Verification Fails:** Ensure that your `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` in `.env` match exactly and belong to the same Razorpay test account.
