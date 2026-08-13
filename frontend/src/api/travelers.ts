import { travelerClient } from './client';
import type { ApiResponse, PagedResponse, Traveler } from '../types';

export interface CreateTravelerData {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  nationality?: string;
  passportNumber?: string;
  passportExpiry?: string;
}

export const travelersApi = {
  create: async (data: CreateTravelerData): Promise<Traveler> => {
    const res = await travelerClient.post<ApiResponse<Traveler>>('/travelers', data);
    return res.data.data;
  },

  getById: async (id: string): Promise<Traveler> => {
    const res = await travelerClient.get<ApiResponse<Traveler>>(`/travelers/${id}`);
    return res.data.data;
  },

  getAll: async (page = 0, size = 20): Promise<PagedResponse<Traveler>> => {
    const res = await travelerClient.get<ApiResponse<PagedResponse<Traveler>>>('/travelers', {
      params: { page, size },
    });
    return res.data.data;
  },

  update: async (id: string, data: Partial<CreateTravelerData>): Promise<Traveler> => {
    const res = await travelerClient.put<ApiResponse<Traveler>>(`/travelers/${id}`, data);
    return res.data.data;
  },
};
