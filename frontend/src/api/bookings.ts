import { bookingClient } from './client';
import type { ApiResponse, PagedResponse, Trip, Booking, SearchResult } from '../types';

export const tripsApi = {
  create: async (data: { travelerId: string; name: string; destination: string; startDate: string; endDate: string; description?: string }): Promise<Trip> => {
    const res = await bookingClient.post<ApiResponse<Trip>>('/trips', data);
    return res.data.data;
  },

  getById: async (id: string): Promise<Trip> => {
    const res = await bookingClient.get<ApiResponse<Trip>>(`/trips/${id}`);
    return res.data.data;
  },

  getByTraveler: async (travelerId: string): Promise<PagedResponse<Trip>> => {
    const res = await bookingClient.get<ApiResponse<PagedResponse<Trip>>>('/trips', {
      params: { travelerId },
    });
    return res.data.data;
  },
};

export const bookingsApi = {
  create: async (data: {
    tripId: string;
    travelerId: string;
    currency?: string;
    items: Array<{
      itemType: string;
      supplierCode?: string;
      origin?: string;
      destination?: string;
      departureDate?: string;
      returnDate?: string;
      passengers?: number;
      pricePerUnit?: number;
      quantity?: number;
      currency?: string;
    }>;
  }): Promise<Booking> => {
    const res = await bookingClient.post<ApiResponse<Booking>>('/bookings', data);
    return res.data.data;
  },

  getById: async (id: string): Promise<Booking> => {
    const res = await bookingClient.get<ApiResponse<Booking>>(`/bookings/${id}`);
    return res.data.data;
  },

  getStatus: async (id: string): Promise<Booking> => {
    const res = await bookingClient.get<ApiResponse<Booking>>(`/bookings/${id}/status`);
    return res.data.data;
  },

  getByTraveler: async (travelerId: string): Promise<PagedResponse<Booking>> => {
    const res = await bookingClient.get<ApiResponse<PagedResponse<Booking>>>('/bookings', {
      params: { travelerId },
    });
    return res.data.data;
  },

  search: async (data: { origin: string; destination: string; departureDate: string; passengers: number; includeFlights: boolean; includeHotels: boolean; includeCars: boolean }): Promise<SearchResult> => {
    const res = await bookingClient.post<ApiResponse<SearchResult>>('/search', data);
    return res.data.data;
  },
};
