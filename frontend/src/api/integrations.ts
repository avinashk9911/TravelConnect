import { integrationClient } from './client';
import type { ApiResponse, IntegrationRequest } from '../types';

export const integrationsApi = {
  getById: async (id: string): Promise<IntegrationRequest> => {
    const res = await integrationClient.get<ApiResponse<IntegrationRequest>>(`/integrations/${id}`);
    return res.data.data;
  },

  getByBooking: async (bookingId: string): Promise<IntegrationRequest[]> => {
    const res = await integrationClient.get<ApiResponse<IntegrationRequest[]>>(`/integrations/booking/${bookingId}`);
    return res.data.data;
  },

  getSupplierStatus: async (): Promise<Array<{ supplierId: string; supplierType: string; available: boolean }>> => {
    const res = await integrationClient.get<ApiResponse<Array<{ supplierId: string; supplierType: string; available: boolean }>>>('/integrations/suppliers');
    return res.data.data;
  },
};
