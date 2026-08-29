import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Calendar, Ticket } from "lucide-react";
import { apiClient } from "../services/apiClient";
import type { EventResponse } from "../types/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export const Events: React.FC = () => {
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await apiClient.get<EventResponse[]>("/events");
        setEvents(response.data);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else {
          setError("Failed to load events. Please try again later.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchEvents();
  }, []);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="container mx-auto px-4 py-12 md:py-16">
      <div className="mb-12 text-center max-w-2xl mx-auto">
        <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl mb-4">Discover Events</h1>
        <p className="text-xl text-muted-foreground">
          Find the best experiences from concerts to conferences. Book your tickets instantly and securely.
        </p>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
            <Card key={n} className="overflow-hidden flex flex-col h-full">
              <Skeleton className="h-48 w-full rounded-none" />
              <CardHeader>
                <Skeleton className="h-6 w-3/4 mb-2" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent className="flex-1">
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-2/3" />
              </CardContent>
              <CardFooter>
                <Skeleton className="h-10 w-full" />
              </CardFooter>
            </Card>
          ))}
        </div>
      ) : error ? (
        <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-8 text-center text-destructive max-w-2xl mx-auto">
          {error}
        </div>
      ) : events.length === 0 ? (
        <div className="rounded-lg border border-border bg-card p-16 text-center flex flex-col items-center justify-center max-w-2xl mx-auto">
          <Ticket className="h-16 w-16 mb-6 text-muted-foreground opacity-50" />
          <h3 className="text-2xl font-semibold text-foreground mb-2">No events available</h3>
          <p className="text-muted-foreground text-lg">Check back later for exciting new shows and experiences.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {events.map((event) => (
            <Card key={event.id} className="overflow-hidden flex flex-col h-full transition-all duration-300 hover:shadow-lg hover:-translate-y-1 group border-border/50">
              <div
                className="h-48 w-full bg-cover bg-center transition-transform duration-500 group-hover:scale-105"
                style={{
                  backgroundImage: event.bannerUrl ? `url(${event.bannerUrl})` : "linear-gradient(135deg, hsl(var(--primary)), hsl(var(--primary)/0.6))",
                }}
              />
              <CardHeader className="pb-4 relative z-10 bg-card">
                <CardTitle className="line-clamp-1">{event.title}</CardTitle>
                <CardDescription className="flex items-center mt-1">
                  <Calendar className="mr-2 h-4 w-4" />
                  <span className="truncate">{formatDate(event.startTime)}</span>
                </CardDescription>
              </CardHeader>
              <CardContent className="flex-1 pb-4 relative z-10 bg-card">
                <p className="text-sm text-muted-foreground line-clamp-3">
                  {event.description}
                </p>
              </CardContent>
              <CardFooter className="pt-0 flex justify-between items-center relative z-10 bg-card border-t border-border/10 pt-4">
                <div className="flex flex-col">
                  <span className="text-xs text-muted-foreground">Starting from</span>
                  <span className="font-semibold text-foreground">₹{event.basePrice.toFixed(2)}</span>
                </div>
                <Link to={`/events/${event.id}`}>
                  <Button variant="default">Book Tickets</Button>
                </Link>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
