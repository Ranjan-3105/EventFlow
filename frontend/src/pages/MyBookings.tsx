import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Ticket, Calendar, AlertCircle } from "lucide-react";
import { apiClient } from "../services/apiClient";
import type { BookingResponse } from "../types/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";

export const MyBookings: React.FC = () => {
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBookings = async () => {
      try {
        const response = await apiClient.get<BookingResponse[]>("/bookings");

        // Sort bookings by ID (or you could sort by date if the backend provided it)
        // Since we don't have a date in BookingResponse, we'll just reverse the array
        // to show newest first assuming sequential IDs or creation order.
        setBookings(response.data.reverse());
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else {
          setError("Failed to load your bookings.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchBookings();
  }, []);

  const getBadgeVariant = (status: string) => {
    switch (status) {
      case "CONFIRMED": return "default";
      case "PENDING": return "secondary";
      case "CANCELLED": return "destructive";
      case "EXPIRED": return "outline";
      default: return "outline";
    }
  };

  return (
    <div className="container mx-auto px-4 py-12 md:py-16 max-w-4xl">
      <div className="mb-10 text-center md:text-left">
        <h1 className="text-3xl font-bold tracking-tight mb-2">My Bookings</h1>
        <p className="text-muted-foreground">Manage and view all your past and upcoming event tickets.</p>
      </div>

      {isLoading ? (
        <div className="space-y-6">
          {[1, 2, 3].map((n) => (
            <Card key={n} className="overflow-hidden">
              <CardHeader className="pb-4">
                <Skeleton className="h-6 w-1/4 mb-2" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent>
                <div className="flex gap-8">
                  <Skeleton className="h-10 w-20" />
                  <Skeleton className="h-10 w-24" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : error ? (
        <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-8 text-center text-destructive max-w-2xl mx-auto">
          <AlertCircle className="h-10 w-10 mx-auto mb-4 opacity-80" />
          {error}
        </div>
      ) : bookings.length === 0 ? (
        <div className="rounded-lg border border-border bg-card p-16 text-center flex flex-col items-center justify-center max-w-2xl mx-auto shadow-sm">
          <Ticket className="h-16 w-16 mb-6 text-muted-foreground opacity-30" />
          <h3 className="text-2xl font-semibold text-foreground mb-2">No bookings found</h3>
          <p className="text-muted-foreground mb-8">
            You haven't made any bookings yet. Discover our amazing events and book your first ticket!
          </p>
          <Link to="/events">
            <Button size="lg" className="px-8 h-12 text-base font-semibold">
              Explore Events
            </Button>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          {bookings.map((booking) => (
            <Card key={booking.bookingId} className="overflow-hidden transition-all duration-200 hover:shadow-md border-border/60">
              <CardHeader className="flex flex-col sm:flex-row justify-between items-start sm:items-center bg-muted/20 pb-4 border-b border-border/40 gap-4">
                <div>
                  <div className="text-xs text-muted-foreground font-semibold tracking-wider uppercase mb-1">
                    Reference ID: {booking.bookingId}
                  </div>
                  <CardTitle className="text-xl">
                    <Link to={`/events/${booking.eventId}`} className="hover:text-primary transition-colors flex items-center gap-2">
                      <Calendar className="h-5 w-5 text-muted-foreground" />
                      Event #{booking.eventId.substring(0, 8)}...
                    </Link>
                  </CardTitle>
                </div>
                <Badge variant={getBadgeVariant(booking.status)} className="px-3 py-1 text-xs">
                  {booking.status}
                </Badge>
              </CardHeader>

              <CardContent className="p-6">
                <div className="flex flex-wrap gap-x-12 gap-y-6">
                  <div className="flex flex-col">
                    <span className="text-sm text-muted-foreground mb-1">Tickets</span>
                    <div className="flex items-center gap-2">
                      <Ticket className="h-5 w-5 text-primary opacity-80" />
                      <span className="text-2xl font-bold">{booking.seatCount}</span>
                    </div>
                  </div>

                  <Separator orientation="vertical" className="hidden sm:block h-12" />

                  <div className="flex flex-col">
                    <span className="text-sm text-muted-foreground mb-1">Total Amount</span>
                    <span className="text-2xl font-bold">₹{booking.totalAmount.toFixed(2)}</span>
                  </div>
                </div>
              </CardContent>

              <CardFooter className="bg-muted/10 border-t border-border/40 pt-4 flex justify-end">
                <Link to={`/events/${booking.eventId}`}>
                  <Button variant="outline" size="sm">
                    View Event Details
                  </Button>
                </Link>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
