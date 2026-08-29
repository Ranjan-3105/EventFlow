
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { ThemeProvider } from "@/components/theme-provider";
import { Navbar } from "@/components/navbar";
import {
  Login,
  Register,
  Home,
  Events,
  EventDetails,
  SeatSelection,
  BookingPayment,
  BookingSuccess,
  MyBookings,
  BookingDetails,
} from "./pages";

function App() {
  return (
    <ThemeProvider defaultTheme="system" storageKey="eventflow-theme">
      <AuthProvider>
        <Router>
          <div className="flex min-h-screen flex-col">
            <Navbar />
            <main className="flex-1">
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/events" element={<Events />} />
                <Route path="/events/:eventId" element={<EventDetails />} />

                {/* Protected Routes */}
                <Route element={<ProtectedRoute />}>
                  <Route path="/events/:eventId/seats" element={<SeatSelection />} />
                  <Route path="/checkout" element={<BookingPayment />} />
                  <Route path="/success" element={<BookingSuccess />} />
                  <Route path="/bookings" element={<MyBookings />} />
                  <Route path="/bookings/:bookingId" element={<BookingDetails />} />
                </Route>
              </Routes>
            </main>
          </div>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
