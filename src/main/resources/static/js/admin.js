/* ═══════════════════════════════════════════════════════
   admin.js  —  Admin Utilities for Grand Hotel
   ═══════════════════════════════════════════════════════ */

/* ── MINI BAR CHART ────────────────────────────────────── */
/**
 * Renders a simple SVG bar chart inside a container element.
 * @param {string} containerId  - DOM element id
 * @param {Array}  data         - [{ label, value }]
 * @param {string} color        - bar fill color (default gold)
 */
function renderBarChart(containerId, data, color = '#c9a84c') {
  const el = document.getElementById(containerId);
  if (!el || !data.length) return;

  const W = el.offsetWidth || 400;
  const H = 180;
  const PADDING = { top: 20, right: 16, bottom: 40, left: 48 };
  const chartW = W - PADDING.left - PADDING.right;
  const chartH = H - PADDING.top - PADDING.bottom;

  const maxVal = Math.max(...data.map(d => d.value), 1);
  const barW   = Math.floor(chartW / data.length) - 6;

  const bars = data.map((d, i) => {
    const barH  = Math.floor((d.value / maxVal) * chartH);
    const x     = PADDING.left + i * (chartW / data.length) + 3;
    const y     = PADDING.top + chartH - barH;
    const yText = y - 6;
    return `
      <rect x="${x}" y="${y}" width="${barW}" height="${barH}"
        fill="${color}" rx="3" opacity=".85"/>
      <text x="${x + barW / 2}" y="${yText}" text-anchor="middle"
        font-size="9" fill="#374151">${d.value}</text>
      <text x="${x + barW / 2}" y="${H - 8}" text-anchor="middle"
        font-size="9" fill="#9ca3af">${d.label}</text>`;
  }).join('');

  // Y-axis ticks
  const ticks = [0, 0.25, 0.5, 0.75, 1].map(t => {
    const val  = Math.round(t * maxVal);
    const y    = PADDING.top + chartH - t * chartH;
    return `
      <line x1="${PADDING.left}" y1="${y}" x2="${W - PADDING.right}" y2="${y}"
        stroke="#f3f4f6" stroke-width="1"/>
      <text x="${PADDING.left - 4}" y="${y + 4}" text-anchor="end"
        font-size="9" fill="#9ca3af">${val}</text>`;
  }).join('');

  el.innerHTML = `
    <svg width="100%" height="${H}" viewBox="0 0 ${W} ${H}"
      xmlns="http://www.w3.org/2000/svg">
      ${ticks}${bars}
    </svg>`;
}

/* ── EXPORT TO CSV ─────────────────────────────────────── */
function exportToCSV(data, filename = 'export.csv') {
  if (!data || !data.length) { showToast('No data to export', 'error'); return; }

  const headers = Object.keys(data[0]);
  const rows    = data.map(row =>
    headers.map(h => {
      let val = row[h] ?? '';
      if (typeof val === 'string' && (val.includes(',') || val.includes('"'))) {
        val = `"${val.replace(/"/g, '""')}"`;
      }
      return val;
    }).join(','));

  const csv  = [headers.join(','), ...rows].join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url  = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href  = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
  showToast(`Exported ${data.length} rows to ${filename}`, 'success');
}

/* ── EXPORT BOOKINGS ───────────────────────────────────── */
function exportBookings(bookings) {
  const rows = bookings.map(b => ({
    Reference:    b.bookingReference,
    Guest:        b.guestName,
    Email:        b.guestEmail,
    Room:         b.roomNumber,
    RoomType:     b.roomType,
    CheckIn:      b.checkInDate,
    CheckOut:     b.checkOutDate,
    Nights:       b.numberOfNights,
    Guests:       b.numberOfGuests,
    Total:        b.totalAmount,
    Advance:      b.advancePayment,
    Balance:      b.balanceAmount,
    Status:       b.bookingStatus,
    BookedOn:     b.createdAt?.split('T')[0] || ''
  }));
  exportToCSV(rows, `bookings_${new Date().toISOString().split('T')[0]}.csv`);
}

/* ── EXPORT PAYMENTS ───────────────────────────────────── */
function exportPayments(payments) {
  const rows = payments.map(p => ({
    TransactionId:  p.transactionId,
    BookingRef:     p.bookingReference,
    Amount:         p.amount,
    Method:         p.paymentMethod,
    Type:           p.paymentType,
    Status:         p.paymentStatus,
    Date:           p.paymentDate?.split('T')[0] || ''
  }));
  exportToCSV(rows, `payments_${new Date().toISOString().split('T')[0]}.csv`);
}

/* ── PRINT BOOKING RECEIPT ─────────────────────────────── */
function printBookingReceipt(booking) {
  const w = window.open('', '_blank', 'width=700,height=600');
  w.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>Booking Receipt — ${booking.bookingReference}</title>
      <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: Arial, sans-serif; padding: 40px; color: #1a1a2e; }
        .header { text-align: center; border-bottom: 2px solid #c9a84c; padding-bottom: 20px; margin-bottom: 24px; }
        .hotel-name { font-size: 1.8rem; font-weight: 700; color: #1a1a2e; }
        .gold { color: #c9a84c; }
        .ref { font-family: monospace; font-size: 1.1rem; font-weight: 700; color: #92400e;
               background: #fef3c7; padding: 6px 14px; border-radius: 4px; display: inline-block; margin: 12px 0; }
        .row { display: flex; justify-content: space-between; padding: 10px 0;
               border-bottom: 1px solid #f3f4f6; font-size: 0.9rem; }
        .row label { color: #6b7280; }
        .row span { font-weight: 600; }
        .total-row { font-size: 1.1rem; font-weight: 700; margin-top: 16px;
                     padding-top: 16px; border-top: 2px solid #1a1a2e; display: flex; justify-content: space-between; }
        .footer { text-align: center; margin-top: 32px; font-size: 0.8rem; color: #9ca3af; }
        @media print { body { padding: 20px; } }
      </style>
    </head>
    <body>
      <div class="header">
        <div class="hotel-name">⬡ Grand<span class="gold">Hotel</span></div>
        <div style="color:#6b7280;font-size:.85rem;margin-top:4px">123 Palace Road, Mumbai 400001</div>
        <div class="ref">${booking.bookingReference}</div>
        <div style="font-size:.8rem;color:#6b7280">Booking Confirmation Receipt</div>
      </div>
      <div class="row"><label>Guest Name</label><span>${booking.guestName}</span></div>
      <div class="row"><label>Email</label><span>${booking.guestEmail}</span></div>
      <div class="row"><label>Room</label><span>${booking.roomNumber} (${booking.roomType})</span></div>
      <div class="row"><label>Check-In</label><span>${booking.checkInDate}</span></div>
      <div class="row"><label>Check-Out</label><span>${booking.checkOutDate}</span></div>
      <div class="row"><label>Nights</label><span>${booking.numberOfNights}</span></div>
      <div class="row"><label>Guests</label><span>${booking.numberOfGuests}</span></div>
      <div class="row"><label>Rate/Night</label><span>₹${booking.pricePerNight?.toLocaleString('en-IN')}</span></div>
      <div class="row"><label>Advance Paid</label><span>₹${(booking.advancePayment||0).toLocaleString('en-IN')}</span></div>
      <div class="row"><label>Balance Due</label><span>₹${(booking.balanceAmount||0).toLocaleString('en-IN')}</span></div>
      <div class="row"><label>Status</label><span>${booking.bookingStatus}</span></div>
      <div class="total-row"><label>Total Amount</label><span>₹${booking.totalAmount?.toLocaleString('en-IN')}</span></div>
      <div class="footer">
        Thank you for choosing Grand Hotel · support@grandhotel.com · +91 22 1234 5678<br>
        Printed on ${new Date().toLocaleString('en-IN')}
      </div>
    </body>
    </html>`);
  w.document.close();
  w.print();
}

/* ── REVENUE CHART DATA BUILDER ────────────────────────── */
function buildMonthlyRevenueData(payments) {
  const months = ['Jan','Feb','Mar','Apr','May','Jun',
                  'Jul','Aug','Sep','Oct','Nov','Dec'];
  const totals = new Array(12).fill(0);

  (payments || []).forEach(p => {
    if (p.paymentStatus !== 'COMPLETED') return;
    const date = new Date(p.paymentDate || p.createdAt);
    if (!isNaN(date)) totals[date.getMonth()] += (p.amount || 0);
  });

  return months.map((label, i) => ({
    label,
    value: Math.round(totals[i] / 1000) // in thousands
  }));
}

/* ── BOOKING STATUS DONUT ──────────────────────────────── */
function renderDonutChart(containerId, data) {
  const el = document.getElementById(containerId);
  if (!el || !data.length) return;

  const total  = data.reduce((s, d) => s + d.value, 0) || 1;
  const radius = 60;
  const cx = 80, cy = 80;
  const colors = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#6b7280'];

  let offset = 0;
  const slices = data.map((d, i) => {
    const pct   = d.value / total;
    const angle = pct * 2 * Math.PI;
    const x1 = cx + radius * Math.sin(offset);
    const y1 = cy - radius * Math.cos(offset);
    offset += angle;
    const x2 = cx + radius * Math.sin(offset);
    const y2 = cy - radius * Math.cos(offset);
    const large = pct > 0.5 ? 1 : 0;
    return `<path d="M${cx},${cy} L${x1},${y1} A${radius},${radius} 0 ${large},1 ${x2},${y2} Z"
              fill="${colors[i % colors.length]}" opacity=".85"/>
            <title>${d.label}: ${d.value}</title>`;
  }).join('');

  // Legend
  const legend = data.map((d, i) => `
    <div style="display:flex;align-items:center;gap:6px;font-size:.75rem;color:#374151">
      <div style="width:10px;height:10px;border-radius:2px;background:${colors[i % colors.length]};flex-shrink:0"></div>
      <span>${d.label}</span>
      <span style="margin-left:auto;font-weight:600;color:#1a1a2e">${d.value}</span>
    </div>`).join('');

  el.innerHTML = `
    <div style="display:flex;align-items:center;gap:16px;flex-wrap:wrap">
      <svg width="160" height="160" viewBox="0 0 160 160" xmlns="http://www.w3.org/2000/svg">
        ${slices}
        <circle cx="${cx}" cy="${cy}" r="30" fill="#fff"/>
        <text x="${cx}" y="${cy + 4}" text-anchor="middle" font-size="11" font-weight="700" fill="#1a1a2e">${total}</text>
        <text x="${cx}" y="${cy + 16}" text-anchor="middle" font-size="8" fill="#9ca3af">Total</text>
      </svg>
      <div style="flex:1;display:flex;flex-direction:column;gap:8px">${legend}</div>
    </div>`;
}
