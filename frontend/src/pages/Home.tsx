import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Calendar, ChevronRight } from "lucide-react";
import { apiClient } from "../services/apiClient";
import { useAuth } from "../context/AuthContext";
import type { EventResponse } from "../types/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export const Home: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [featuredEvents, setFeaturedEvents] = useState<EventResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchFeaturedEvents = async () => {
      try {
        const response = await apiClient.get<EventResponse[]>("/events");
        // Only take the first 3 events for the homepage
        setFeaturedEvents(response.data.slice(0, 3));
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else {
          setError("Failed to load featured events.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchFeaturedEvents();
  }, []);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="relative w-full overflow-hidden bg-background pt-24 pb-32 md:pt-36 md:pb-48 border-b border-border/40">
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]"></div>
        <div className="absolute left-0 right-0 top-0 -z-10 m-auto h-[310px] w-[310px] rounded-full bg-primary/20 opacity-20 blur-[100px]"></div>

        <div className="container relative z-10 mx-auto px-4 text-center max-w-4xl">
          <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl md:text-6xl lg:text-7xl mb-6">
            Discover experiences <br className="hidden sm:block" />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-primary/60">worth remembering.</span>
          </h1>

          <p className="mx-auto max-w-2xl text-lg text-muted-foreground sm:text-xl mb-10 leading-relaxed">
            Book movie nights, live events, and more — with simple seat selection and secure checkout.
            Join EventFlow to explore what's happening.
          </p>

          <div className="flex flex-col sm:flex-row justify-center items-center gap-4">
            <Link to="/events">
              <Button size="lg" className="w-full sm:w-auto h-12 px-8 text-base font-semibold">
                Explore Events
                <ChevronRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>

            {isAuthenticated ? (
              <Link to="/bookings">
                <Button size="lg" variant="outline" className="w-full sm:w-auto h-12 px-8 text-base font-semibold">
                  My Bookings
                </Button>
              </Link>
            ) : (
              <Link to="/register">
                <Button size="lg" variant="outline" className="w-full sm:w-auto h-12 px-8 text-base font-semibold">
                  Create Account
                </Button>
              </Link>
            )}
          </div>
        </div>
      </section>

      {/* Featured Events Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end mb-10">
          <div>
            <h2 className="text-3xl font-bold tracking-tight mb-2">Featured Events</h2>
            <p className="text-muted-foreground">Don't miss out on these popular upcoming experiences.</p>
          </div>
          <Link to="/events" className="mt-4 sm:mt-0 text-primary font-medium hover:underline flex items-center">
            View All Events
            <ChevronRight className="ml-1 h-4 w-4" />
          </Link>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((n) => (
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
          <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-6 text-center text-destructive">
            {error}
          </div>
        ) : featuredEvents.length === 0 ? (
          <div className="rounded-lg border border-border bg-card p-12 text-center text-muted-foreground flex flex-col items-center justify-center">
            <Calendar className="h-12 w-12 mb-4 opacity-50" />
            <h3 className="text-lg font-semibold text-foreground mb-2">No events right now</h3>
            <p>Check back later for exciting shows and experiences.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {featuredEvents.map((event) => (
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
                    {formatDate(event.startTime)}
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
                    <Button variant="default">View Details</Button>
                  </Link>
                </CardFooter>
              </Card>
            ))}
          </div>
        )}
      </section>

      {/* Footer */}
      <footer className="mt-auto border-t py-8 bg-muted/40">
        <div className="container mx-auto px-4 text-center text-sm text-muted-foreground">
          <p>© {new Date().getFullYear()} EventFlow. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};
