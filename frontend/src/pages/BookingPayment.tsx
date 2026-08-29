import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ShieldCheck, AlertCircle, Loader2 } from "lucide-react";
import { apiClient } from "../services/apiClient";
import type {
  EventResponse,
  BookingResponse,
  PaymentResponse,
  CreateBookingRequest,
  CreatePaymentRequest,
  VerifyPaymentRequest,
  SeatAvailabilityResponse
} from "../types/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardFooter, CardDescription } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";

// Helper type to store our frontend display info alongside the backend response
type SeatWithDisplay = SeatAvailabilityResponse & {
  displayLabel: string;
};

export const BookingPayment: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // State from previous page
  const state = location.state as { eventId?: string; seatIds?: string[] } | null;
  const eventId = state?.eventId;
  const seatIds = state?.seatIds;

  const [event, setEvent] = useState<EventResponse | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<SeatWithDisplay[]>([]);
  const [isLoadingDetails, setIsLoadingDetails] = useState(true);

  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load Razorpay script dynamically
  useEffect(() => {
    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    document.body.appendChild(script);
    return () => {
      document.body.removeChild(script);
    };
  }, []);

  // Fetch fresh event details to ensure accurate pricing and labels
  useEffect(() => {
    const fetchDetails = async () => {
      if (!eventId || !seatIds || seatIds.length === 0) {
        setIsLoadingDetails(false);
        return;
      }

      try {
        const [eventRes, seatsRes] = await Promise.all([
          apiClient.get<EventResponse>(`/events/${eventId}`),
          apiClient.get<SeatAvailabilityResponse[]>(`/events/${eventId}/seats`)
        ]);

        setEvent(eventRes.data);

        // Need to calculate global display labels to match SeatSelection exactly
        const allSeats = seatsRes.data;
        const sortedSeats = [...allSeats].sort((a, b) => {
          if (a.rowLabel === b.rowLabel) {
            return a.seatNumber - b.seatNumber;
          }
          return a.rowLabel.localeCompare(b.rowLabel);
        });

        const mappedSeats: SeatWithDisplay[] = sortedSeats.map((seat, index) => {
          const globalDisplayNumber = index + 1;
          return {
            ...seat,
            displayLabel: `${seat.rowLabel}${globalDisplayNumber}`
          };
        });

        const filteredSeats = mappedSeats.filter(s => seatIds.includes(s.seatId));
        setSelectedSeats(filteredSeats);
      } catch {
        setError("Failed to load booking details. Please try again.");
      } finally {
        setIsLoadingDetails(false);
      }
    };

    fetchDetails();
  }, [eventId, seatIds]);

  // Handle missing router state
  if (!isLoadingDetails && (!eventId || !seatIds || seatIds.length === 0)) {
    return (
      <div className="container mx-auto px-4 py-32 text-center flex flex-col items-center max-w-md">
        <AlertCircle className="h-16 w-16 text-muted-foreground mb-4 opacity-50" />
        <h2 className="text-2xl font-bold mb-2">Missing Booking Information</h2>
        <p className="text-muted-foreground mb-8">Please select an event and seats first to proceed to checkout.</p>
        <Button size="lg" className="w-full" onClick={() => navigate("/")}>
          Back to Events
        </Button>
      </div>
    );
  }

  const handleCheckout = async () => {
    if (!eventId || !seatIds || !event) return;

    setError(null);
    setIsProcessing(true);

    try {
      // 1. Create Booking
      const bookingReq: CreateBookingRequest = { eventId, seatIds };
      const bookingRes = await apiClient.post<BookingResponse>("/bookings", bookingReq);
      const booking = bookingRes.data;

      // 2. Create Payment
      const paymentReq: CreatePaymentRequest = { bookingId: booking.bookingId };
      const paymentRes = await apiClient.post<PaymentResponse>("/payments", paymentReq);
      const payment = paymentRes.data;

      // 3. Open Razorpay Checkout
      const options = {
        key: payment.razorpayKeyId,
        amount: payment.amount, // from backend (typically in paise)
        currency: "INR",
        name: "EventFlow",
        description: `Booking for ${event.title}`,
        order_id: payment.razorpayOrderId,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        handler: async function (response: any) {
          try {
            setIsProcessing(true); // Ensure processing stays true during verification

            // 4. Verify Payment
            const verifyReq: VerifyPaymentRequest = {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            };

            await apiClient.post("/payments/verify", verifyReq);

            // 5. Success
            navigate("/success", {
              state: {
                bookingId: booking.bookingId,
                eventTitle: event.title,
                seatCount: booking.seatCount,
                totalAmount: booking.totalAmount,
                seatLabels: selectedSeats.map(s => s.displayLabel).join(', ')
              },
              replace: true
            });
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          } catch (err: any) {
            setError(err.response?.data?.message || "Payment verification failed.");
            setIsProcessing(false);
          }
        },
        modal: {
          ondismiss: function() {
            setIsProcessing(false);
          }
        },
        theme: {
          color: "#4f46e5" // Use indigo-600
        }
      };

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const rzp = new (window as any).Razorpay(options);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      rzp.on('payment.failed', function (response: any) {
        setError(response.error.description || "Payment failed.");
        setIsProcessing(false);
      });

      rzp.open();

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError("The selected seats are no longer available. Please select different seats.");
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError("An unexpected error occurred during checkout.");
      }
      setIsProcessing(false);
    }
  };

  if (isLoadingDetails) {
    return (
      <div className="container mx-auto px-4 py-32 flex justify-center items-center">
        <Loader2 className="h-12 w-12 animate-spin text-primary opacity-50" />
      </div>
    );
  }

  const estimatedTotal = (event?.basePrice || 0) * selectedSeats.length;

  return (
    <div className="container mx-auto px-4 py-12 md:py-16 max-w-lg">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold tracking-tight mb-2">Complete Your Booking</h1>
        <p className="text-muted-foreground">Review your selection and pay securely</p>
      </div>

      {error && (
        <div className="mb-6 rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-center text-sm font-medium text-destructive">
          {error}
        </div>
      )}

      <Card className="border-border/50 shadow-lg overflow-hidden">
        <div className="h-2 w-full bg-gradient-to-r from-primary to-primary/60"></div>
        <CardHeader className="pb-4">
          <CardTitle className="text-xl">{event?.title}</CardTitle>
          <CardDescription>
            {event ? new Date(event.startTime).toLocaleDateString("en-US", { weekday: 'long', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''}
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-6">
          <Separator />

          <div className="space-y-4">
            <div className="flex justify-between items-start">
              <span className="text-muted-foreground text-sm">Selected Seats</span>
              <span className="font-semibold text-right max-w-[60%]">
                {selectedSeats.map(s => s.displayLabel).join(', ')}
              </span>
            </div>

            <div className="flex justify-between items-center">
              <span className="text-muted-foreground text-sm">Number of Tickets</span>
              <span className="font-semibold">{selectedSeats.length}</span>
            </div>

            <div className="flex justify-between items-center">
              <span className="text-muted-foreground text-sm">Price per Ticket</span>
              <span className="font-semibold">₹{event?.basePrice.toFixed(2)}</span>
            </div>
          </div>

          <Separator className="border-dashed" />

          <div className="flex justify-between items-end">
            <span className="text-lg text-muted-foreground font-medium">Total Amount</span>
            <span className="text-3xl font-extrabold text-foreground">₹{estimatedTotal.toFixed(2)}</span>
          </div>
        </CardContent>

        <CardFooter className="flex-col pt-0 pb-8 px-6 gap-4">
          <Button
            size="lg"
            className="w-full h-14 text-lg font-semibold shadow-md"
            onClick={handleCheckout}
            disabled={isProcessing}
          >
            {isProcessing ? (
              <>
                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                Processing Securely...
              </>
            ) : (
              'Pay Securely with Razorpay'
            )}
          </Button>

          <div className="flex items-center justify-center gap-2 text-xs text-muted-foreground">
            <ShieldCheck className="h-4 w-4 text-emerald-500" />
            <span>256-bit secure encryption by Razorpay</span>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
};
