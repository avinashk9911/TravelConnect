export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface Traveler {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  nationality?: string;
  passportNumber?: string;
  passportExpiry?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface Trip {
  id: string;
  travelerId: string;
  name: string;
  description?: string;
  destination: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
}

export interface BookingItem {
  id: string;
  itemType: 'FLIGHT' | 'HOTEL' | 'CAR';
  supplierCode?: string;
  origin?: string;
  destination?: string;
  departureDate?: string;
  returnDate?: string;
  passengers?: number;
  pricePerUnit?: number;
  quantity?: number;
  currency?: string;
}

export interface Booking {
  id: string;
  tripId: string;
  travelerId: string;
  bookingReference: string;
  status: 'PENDING' | 'PROCESSING' | 'CONFIRMED' | 'FAILED' | 'CANCELLED';
  totalAmount?: number;
  currency?: string;
  traceId?: string;
  items: BookingItem[];
  createdAt: string;
  updatedAt?: string;
}

export interface FlightOption {
  supplierCode: string;
  airline: string;
  flightNumber: string;
  origin: string;
  destination: string;
  departureDate: string;
  price: number;
  currency: string;
  availableSeats: number;
}

export interface HotelOption {
  supplierCode: string;
  hotelName: string;
  city: string;
  checkIn: string;
  roomType: string;
  pricePerNight: number;
  currency: string;
  availableRooms: number;
}

export interface CarOption {
  supplierCode: string;
  carType: string;
  pickupLocation: string;
  pricePerDay: number;
  currency: string;
}

export interface SearchResult {
  origin: string;
  destination: string;
  departureDate: string;
  flights: FlightOption[];
  hotels: HotelOption[];
  cars: CarOption[];
}

export interface IntegrationRequest {
  id: string;
  bookingId: string;
  supplierId: string;
  supplierType: string;
  status: string;
  retryCount: number;
  traceId: string;
  createdAt: string;
}
