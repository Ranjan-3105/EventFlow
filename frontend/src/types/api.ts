export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: "USER" | "ORGANIZER" | "ADMIN";
}

export interface AuthResponse {
  token: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
}

export type EventStatus = "DRAFT" | "PUBLISHED" | "CANCELLED" | "COMPLETED";

export interface EventResponse {
  id: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: EventStatus;
  basePrice: number;
  bannerUrl: string | null;
}

export type SeatStatus = "AVAILABLE" | "LOCKED" | "BOOKED";

export interface SeatAvailabilityResponse {
  seatId: string;
  rowLabel: string;
  seatNumber: number;
  seatType: string;
  status: SeatStatus;
}

export type BookingStatus = "PENDING" | "CONFIRMED" | "CANCELLED" | "EXPIRED";

export interface BookingResponse {
  bookingId: string;
  eventId: string;
  status: BookingStatus;
  seatCount: number;
  totalAmount: number;
}

export interface CreateBookingRequest {
  eventId: string;
  seatIds: string[];
}

export type PaymentStatus = "PENDING" | "SUCCESS" | "FAILED";

export interface PaymentResponse {
  paymentId: string;
  bookingId: string;
  amount: number;
  status: PaymentStatus;
  razorpayOrderId: string;
  razorpayKeyId: string;
}

export interface CreatePaymentRequest {
  bookingId: string;
}

export interface VerifyPaymentRequest {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

export interface ValidationErrorResponse extends ErrorResponse {
  fieldErrors: Record<string, string>;
}
