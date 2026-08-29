import React, { useEffect, useState, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Info, AlertCircle } from "lucide-react";
import { apiClient } from "../services/apiClient";
import type { EventResponse, SeatAvailabilityResponse } from "../types/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

// Helper type to store our frontend display info alongside the backend response
type SeatWithDisplay = SeatAvailabilityResponse & {
  displayLabel: string;
};

export const SeatSelection: React.FC = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const [event, setEvent] = useState<EventResponse | null>(null);
  const [seats, setSeats] = useState<SeatAvailabilityResponse[]>([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState<Set<string>>(new Set());

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      if (!eventId) return;

      try {
        const [eventRes, seatsRes] = await Promise.all([
          apiClient.get<EventResponse>(`/events/${eventId}`),
          apiClient.get<SeatAvailabilityResponse[]>(`/events/${eventId}/seats`)
        ]);

        setEvent(eventRes.data);
        setSeats(seatsRes.data);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        if (err.response?.status === 404) {
          setError("Event or seats not found.");
        } else {
          setError(err.response?.data?.message || "Failed to load seat map.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [eventId]);

  const toggleSeatSelection = (seatId: string, status: string) => {
    if (status !== "AVAILABLE") return;

    setSelectedSeatIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(seatId)) {
        newSet.delete(seatId);
      } else {
        newSet.add(seatId);
      }
      return newSet;
    });
  };

  const handleContinue = () => {
    navigate("/checkout", {
      state: {
        eventId,
        seatIds: Array.from(selectedSeatIds)
      }
    });
  };

  // Group seats by row and calculate unique display labels
  const { seatRows, seatsWithDisplay } = useMemo(() => {
    // 1. Sort all seats globally
    const sortedSeats = [...seats].sort((a, b) => {
      if (a.rowLabel === b.rowLabel) {
        return a.seatNumber - b.seatNumber;
      }
      return a.rowLabel.localeCompare(b.rowLabel);
    });

    // 2. Map backend seats to frontend display models
    const mappedSeats: SeatWithDisplay[] = sortedSeats.map((seat, index) => {
      const globalDisplayNumber = index + 1;
      return {
        ...seat,
        displayLabel: `${seat.rowLabel}${globalDisplayNumber}`
      };
    });

    // 3. Group by row for rendering
    const rows = new Map<string, SeatWithDisplay[]>();
    mappedSeats.forEach(seat => {
      const row = seat.rowLabel;
      if (!rows.has(row)) {
        rows.set(row, []);
      }
      rows.get(row)!.push(seat);
    });

    return {
      seatRows: Array.from(rows.entries()),
      seatsWithDisplay: mappedSeats
    };
  }, [seats]);

  const selectedSeatsDetails = seatsWithDisplay.filter(seat => selectedSeatIds.has(seat.seatId));
  const totalEstimatedAmount = event ? selectedSeatIds.size * event.basePrice : 0;

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-12 md:py-16">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div className="lg:col-span-8 flex flex-col items-center">
            <Skeleton className="w-full max-w-2xl h-16 rounded-t-[50%] mb-12" />
            <Skeleton className="w-full h-64 rounded-xl" />
          </div>
          <div className="lg:col-span-4">
            <Skeleton className="w-full h-80 rounded-xl" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !event) {
    return (
      <div className="container mx-auto px-4 py-32 text-center">
        <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-12 max-w-2xl mx-auto">
          <AlertCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-destructive mb-4">Error Loading Seats</h2>
          <p className="text-muted-foreground mb-8">{error || "Event not found."}</p>
          <Button onClick={() => navigate(-1)} variant="default">
            Go Back
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 md:py-12">
      <div className="mb-8 text-center md:text-left">
        <h1 className="text-3xl font-bold tracking-tight mb-2">Select Your Seats</h1>
        <p className="text-muted-foreground">
          {event.title} • {new Date(event.startTime).toLocaleDateString("en-US", { weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 relative items-start">
        {/* Left Column: Seat Map */}
        <div className="lg:col-span-8 overflow-hidden flex flex-col">
          <Card className="border-border/50 shadow-sm overflow-hidden">
            <CardContent className="p-6 md:p-10 overflow-x-auto">

              <div className="w-max min-w-full mx-auto flex flex-col items-center px-2">
                {/* Screen Indicator */}
                <div className="flex flex-col items-center mb-16 w-[600px] max-w-full">
                  <div className="w-full h-12 border-t-[8px] border-primary/20 rounded-t-[100%] bg-gradient-to-b from-primary/5 to-transparent"></div>
                  <span className="text-sm font-semibold tracking-widest text-muted-foreground uppercase mt-2">Stage / Screen</span>
                </div>

                {/* Seat Grid */}
                <div className="flex flex-col gap-4 pb-4">
                  {seatRows.map(([rowLabel, rowSeats]) => (
                    <div key={rowLabel} className="flex items-center gap-4">
                      <div className="w-6 text-center font-bold text-muted-foreground shrink-0">
                        {rowLabel}
                      </div>

                      <div className="flex gap-2">
                        {rowSeats.map((seat) => {
                          const isSelected = selectedSeatIds.has(seat.seatId);
                          const isAvailable = seat.status === "AVAILABLE";
                          const isLocked = seat.status === "LOCKED";
                          const isBooked = seat.status === "BOOKED";

                          return (
                            <button
                              key={seat.seatId}
                              onClick={() => toggleSeatSelection(seat.seatId, seat.status)}
                              disabled={!isAvailable && !isSelected}
                              className={cn(
                                "h-10 w-10 shrink-0 text-xs font-medium rounded-md transition-all flex items-center justify-center border",
                                isAvailable && !isSelected && "bg-card border-border hover:border-primary hover:text-primary cursor-pointer",
                                isSelected && "bg-primary border-primary text-primary-foreground shadow-md ring-2 ring-primary ring-offset-2 ring-offset-background",
                                isLocked && "bg-amber-500/20 border-amber-500/50 text-amber-600 dark:text-amber-400 cursor-not-allowed",
                                isBooked && "bg-muted border-muted text-muted-foreground/50 cursor-not-allowed opacity-60"
                              )}
                              title={seat.displayLabel}
                            >
                              {seat.displayLabel.replace(seat.rowLabel, '')}
                            </button>
                          );
                        })}
                      </div>

                      <div className="w-6 text-center font-bold text-muted-foreground shrink-0">
                        {rowLabel}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </CardContent>

            {/* Legend */}
            <CardFooter className="bg-muted/30 border-t border-border/50 py-4 flex flex-wrap justify-center gap-6 text-sm">
              <div className="flex items-center gap-2">
                <div className="h-4 w-4 rounded bg-card border border-border"></div>
                <span className="text-muted-foreground">Available</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-4 w-4 rounded bg-primary border border-primary"></div>
                <span className="text-foreground font-medium">Selected</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-4 w-4 rounded bg-amber-500/20 border border-amber-500/50"></div>
                <span className="text-muted-foreground">Locked</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-4 w-4 rounded bg-muted border border-muted opacity-60"></div>
                <span className="text-muted-foreground">Booked</span>
              </div>
            </CardFooter>
          </Card>
        </div>

        {/* Right Column: Booking Summary */}
        <div className="lg:col-span-4">
          <div className="sticky top-24">
            <Card className="border-border/50 shadow-lg">
              <CardHeader className="bg-muted/30 border-b border-border/50">
                <CardTitle>Booking Summary</CardTitle>
              </CardHeader>

              <CardContent className="p-6">
                {selectedSeatIds.size === 0 ? (
                  <div className="flex flex-col items-center justify-center py-8 text-center text-muted-foreground">
                    <Info className="h-10 w-10 mb-3 opacity-20" />
                    <p>Please select seats from the map to continue.</p>
                  </div>
                ) : (
                  <div className="space-y-6">
                    <div>
                      <h4 className="text-sm font-medium text-muted-foreground mb-3">Selected Seats</h4>
                      <div className="flex flex-wrap gap-2">
                        {selectedSeatsDetails.map(seat => (
                          <div key={seat.seatId} className="px-3 py-1 bg-primary/10 text-primary border border-primary/20 rounded-full text-sm font-semibold">
                            {seat.displayLabel}
                          </div>
                        ))}
                      </div>
                    </div>

                    <Separator />

                    <div className="space-y-3">
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Tickets</span>
                        <span className="font-medium">{selectedSeatIds.size}</span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Price per ticket</span>
                        <span className="font-medium">₹{event.basePrice.toFixed(2)}</span>
                      </div>
                    </div>

                    <Separator />

                    <div className="flex justify-between items-end">
                      <span className="text-lg font-semibold">Total</span>
                      <span className="text-2xl font-bold text-primary">₹{totalEstimatedAmount.toFixed(2)}</span>
                    </div>
                  </div>
                )}
              </CardContent>

              <CardFooter className="p-6 pt-0">
                <Button
                  size="lg"
                  className="w-full h-12 text-lg font-semibold"
                  disabled={selectedSeatIds.size === 0}
                  onClick={handleContinue}
                >
                  Continue to Payment
                </Button>
              </CardFooter>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};
