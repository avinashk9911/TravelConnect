import axios from 'axios';

const travelerClient = axios.create({
  baseURL: '/traveler-api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

const bookingClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

const integrationClient = axios.create({
  baseURL: '/integration-api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Response interceptor — unwrap ApiResponse<T>
[travelerClient, bookingClient, integrationClient].forEach(client => {
  client.interceptors.response.use(
    (response) => response,
    (error) => {
      const msg = error.response?.data?.message ?? error.message ?? 'An error occurred';
      return Promise.reject(new Error(msg));
    }
  );
});

export { travelerClient, bookingClient, integrationClient };
