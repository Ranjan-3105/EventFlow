import React from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";

export const BookingSuccess: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // State from previous page
  const state = location.state as {
    bookingId?: string;
    eventTitle?: string;
    seatCount?: number;
    totalAmount?: number;
    seatLabels?: string;
  } | null;

  if (!state || !state.bookingId) {
    return (
      <div className="container mx-auto px-4 py-32 text-center flex flex-col items-center max-w-md">
        <div className="rounded-lg border border-border bg-card p-12 w-full shadow-sm">
          <h2 className="text-2xl font-bold mb-4">No Booking Found</h2>
          <p className="text-muted-foreground mb-8">It seems you navigated here directly or the session expired.</p>
          <Button size="lg" className="w-full" onClick={() => navigate("/")}>
            Back to Home
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-16 md:py-24 flex justify-center items-center">
      <Card className="w-full max-w-md border-border/50 shadow-xl overflow-hidden relative">
        <div className="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-emerald-400 to-emerald-600"></div>

        <CardHeader className="pt-10 pb-6 text-center flex flex-col items-center">
          <div className="w-20 h-20 bg-emerald-100 dark:bg-emerald-900/30 rounded-full flex items-center justify-center mb-6">
            <CheckCircle2 className="h-10 w-10 text-emerald-600 dark:text-emerald-400" />
          </div>
          <CardTitle className="text-3xl font-extrabold text-foreground mb-2">Booking Confirmed!</CardTitle>
          <p className="text-muted-foreground text-center">
            Your tickets have been successfully booked and sent to your email.
          </p>
        </CardHeader>

        <CardContent className="space-y-6 bg-muted/20 pb-8 pt-6 border-t border-border/30">
          <div className="space-y-4 px-2">
            <div className="flex justify-between items-start">
              <span className="text-muted-foreground font-medium">Event</span>
              <span className="font-semibold text-right max-w-[60%]">{state.eventTitle}</span>
            </div>

            <div className="flex justify-between items-start">
              <span className="text-muted-foreground font-medium">Seats ({state.seatCount})</span>
              <span className="font-semibold text-right max-w-[60%]">{state.seatLabels}</span>
            </div>

            <Separator className="my-4 border-dashed" />

            <div className="flex justify-between items-center bg-background rounded-md p-4 border border-border shadow-sm">
              <div className="flex flex-col">
                <span className="text-xs text-muted-foreground mb-1 uppercase tracking-wider font-semibold">Booking ID</span>
                <span className="font-mono font-bold text-sm">{state.bookingId}</span>
              </div>
            </div>

            <div className="flex justify-between items-center mt-6">
              <span className="text-muted-foreground font-medium">Total Paid</span>
              <span className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">
                ₹{state.totalAmount?.toFixed(2)}
              </span>
            </div>
          </div>
        </CardContent>

        <CardFooter className="flex-col gap-3 pt-6 pb-8 px-8 border-t border-border/30">
          <Link to="/bookings" className="w-full">
            <Button size="lg" className="w-full h-12 text-base font-semibold">
              View My Bookings
            </Button>
          </Link>
          <Link to="/events" className="w-full">
            <Button size="lg" variant="outline" className="w-full h-12 text-base font-semibold">
              Explore More Events
            </Button>
          </Link>
        </CardFooter>
      </Card>
    </div>
  );
};
