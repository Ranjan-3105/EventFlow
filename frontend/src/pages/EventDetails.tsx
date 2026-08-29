import React, { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { Calendar, Clock, Ticket } from "lucide-react";
import { apiClient } from "../services/apiClient";
import type { EventResponse } from "../types/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";

export const EventDetails: React.FC = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const [event, setEvent] = useState<EventResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvent = async () => {
      if (!eventId) return;

      try {
        const response = await apiClient.get<EventResponse>(`/events/${eventId}`);
        setEvent(response.data);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        if (err.response?.status === 404) {
          setError("Event not found.");
        } else if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else {
          setError("Failed to load event details. Please try again later.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchEvent();
  }, [eventId]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-12 md:py-16">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12">
          <div className="lg:col-span-8">
            <Skeleton className="w-full aspect-[21/9] rounded-xl mb-8" />
            <Skeleton className="h-10 w-48 mb-4" />
            <Skeleton className="h-4 w-full mb-2" />
            <Skeleton className="h-4 w-full mb-2" />
            <Skeleton className="h-4 w-3/4" />
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
          <h2 className="text-2xl font-bold text-destructive mb-4">Error Loading Event</h2>
          <p className="text-muted-foreground mb-8">{error || "Event not found."}</p>
          <Button onClick={() => navigate("/events")} variant="default">
            Back to Events
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 md:py-12">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12">
        {/* Left Column: Banner & Description */}
        <div className="lg:col-span-8 flex flex-col">
          <div
            className="w-full aspect-[21/9] rounded-xl overflow-hidden bg-muted mb-8 shadow-md"
          >
            {event.bannerUrl ? (
              <img
                src={event.bannerUrl}
                alt={event.title}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full bg-gradient-to-br from-primary/80 to-primary/40 flex items-center justify-center">
                <Ticket className="h-24 w-24 text-white/50" />
              </div>
            )}
          </div>

          <div className="hidden lg:block">
            <h2 className="text-2xl font-bold tracking-tight mb-4">About this event</h2>
            <Separator className="mb-6" />
            <div className="prose prose-slate dark:prose-invert max-w-none">
              <p className="text-muted-foreground leading-relaxed whitespace-pre-wrap">
                {event.description}
              </p>
            </div>
          </div>
        </div>

        {/* Right Column: Sticky Ticket Info */}
        <div className="lg:col-span-4">
          <div className="sticky top-24">
            <Card className="border-border/50 shadow-lg">
              <CardContent className="p-6 md:p-8 flex flex-col gap-6">
                <div>
                  <Badge variant={event.status === 'PUBLISHED' ? 'default' : 'secondary'} className="mb-4">
                    {event.status}
                  </Badge>
                  <h1 className="text-3xl font-bold tracking-tight mb-4">{event.title}</h1>
                </div>

                <div className="flex flex-col gap-4 text-muted-foreground">
                  <div className="flex items-start gap-3">
                    <Calendar className="h-5 w-5 mt-0.5 text-foreground" />
                    <div>
                      <p className="font-medium text-foreground">{formatDate(event.startTime)}</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3">
                    <Clock className="h-5 w-5 mt-0.5 text-foreground" />
                    <div>
                      <p className="font-medium text-foreground">{formatTime(event.startTime)} - {formatTime(event.endTime)}</p>
                    </div>
                  </div>
                </div>

                <Separator />

                <div className="flex justify-between items-end">
                  <div className="flex flex-col">
                    <span className="text-sm text-muted-foreground mb-1">Ticket Price</span>
                    <span className="text-3xl font-bold text-foreground">₹{event.basePrice.toFixed(2)}</span>
                  </div>
                </div>

                <Link to={`/events/${event.id}/seats`} className="w-full mt-2">
                  <Button size="lg" className="w-full h-14 text-lg font-semibold">
                    Select Seats
                  </Button>
                </Link>

                <p className="text-center text-xs text-muted-foreground">
                  Secure checkout provided by Razorpay
                </p>
              </CardContent>
            </Card>

            {/* Mobile Description (shows below the card on mobile, hidden on desktop) */}
            <div className="mt-12 lg:hidden">
              <h2 className="text-2xl font-bold tracking-tight mb-4">About this event</h2>
              <Separator className="mb-6" />
              <div className="prose prose-slate dark:prose-invert max-w-none">
                <p className="text-muted-foreground leading-relaxed whitespace-pre-wrap">
                  {event.description}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
