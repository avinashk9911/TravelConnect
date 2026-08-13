import { useState } from 'react';
import { bookingsApi } from '../api/bookings';
import type { SearchResult, FlightOption, HotelOption, CarOption } from '../types';

export function SearchTravel() {
  const [form, setForm] = useState({
    origin: '', destination: '', departureDate: '', passengers: 1,
    includeFlights: true, includeHotels: true, includeCars: false,
  });
  const [results, setResults] = useState<SearchResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const r = await bookingsApi.search({ ...form, passengers: Number(form.passengers) });
      setResults(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Search failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Search Travel</h1>
      <div className="card">
        <form onSubmit={handleSearch}>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">From (Airport code) *</label>
              <input className="form-input" name="origin" placeholder="LHR" value={form.origin} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">To (Airport code) *</label>
              <input className="form-input" name="destination" placeholder="JFK" value={form.destination} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">Departure Date *</label>
              <input className="form-input" type="date" name="departureDate" value={form.departureDate} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">Passengers</label>
              <input className="form-input" type="number" name="passengers" min={1} max={9} value={form.passengers} onChange={handleChange} />
            </div>
          </div>
          <div style={{ display: 'flex', gap: '1.5rem', margin: '0.5rem 0' }}>
            <label><input type="checkbox" name="includeFlights" checked={form.includeFlights} onChange={handleChange} /> &nbsp;Flights</label>
            <label><input type="checkbox" name="includeHotels" checked={form.includeHotels} onChange={handleChange} /> &nbsp;Hotels</label>
            <label><input type="checkbox" name="includeCars" checked={form.includeCars} onChange={handleChange} /> &nbsp;Cars</label>
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Searching...' : 'Search'}
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {results && (
        <>
          {results.flights?.length > 0 && (
            <div className="card">
              <div className="card-title">Flights ({results.flights.length} results)</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {results.flights.map((f: FlightOption, i: number) => (
                  <div key={i} className="result-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong>{f.airline}</strong> {f.flightNumber}<br />
                        <span style={{ fontSize: '0.875rem', color: '#718096' }}>
                          {f.origin} &#8594; {f.destination} | {f.departureDate} | {f.availableSeats} seats
                        </span>
                      </div>
                      <div className="result-price">{f.currency} {f.price}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {results.hotels?.length > 0 && (
            <div className="card">
              <div className="card-title">Hotels ({results.hotels.length} results)</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {results.hotels.map((h: HotelOption, i: number) => (
                  <div key={i} className="result-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong>{h.hotelName}</strong><br />
                        <span style={{ fontSize: '0.875rem', color: '#718096' }}>
                          {h.city} | {h.roomType} | {h.availableRooms} rooms
                        </span>
                      </div>
                      <div className="result-price">{h.currency} {h.pricePerNight}/night</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {results.cars?.length > 0 && (
            <div className="card">
              <div className="card-title">Cars ({results.cars.length} results)</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {results.cars.map((c: CarOption, i: number) => (
                  <div key={i} className="result-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong>{c.carType}</strong><br />
                        <span style={{ fontSize: '0.875rem', color: '#718096' }}>{c.pickupLocation}</span>
                      </div>
                      <div className="result-price">{c.currency} {c.pricePerDay}/day</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
