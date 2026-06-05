// ── ROOM DATA ──────────────────────────────────────────
const ROOMS = [
  {
    id: 1, number: "101", type: "Single",
    name: "Cozy Single Room",
    desc: "Perfect for solo travellers. Minimalist design with city views and premium bedding.",
    price: 1200, capacity: 1, floor: 1, size: 200,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "Smart TV", "Mini Fridge"],
    bg: "ri-single",
    image: "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80",
    badge: "AVAILABLE"
  },
  {
    id: 2, number: "103", type: "Double",
    name: "Superior Double Room",
    desc: "Spacious king-bed room with panoramic views and luxury bathroom amenities.",
    price: 2500, capacity: 2, floor: 1, size: 320,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "Smart TV", "Mini Bar", "Safe"],
    bg: "ri-double",
    image: "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80",
    badge: "POPULAR"
  },
  {
    id: 3, number: "201", type: "Twin",
    name: "Twin Comfort Room",
    desc: "Two premium single beds, ideal for friends or colleagues travelling together.",
    price: 2800, capacity: 2, floor: 2, size: 300,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "Smart TV", "Work Desk", "Pool Access"],
    bg: "ri-double",
    image: "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80",
    badge: "AVAILABLE"
  },
  {
    id: 4, number: "202", type: "Deluxe",
    name: "Deluxe Sea View",
    desc: "Stunning sea view with a lavish bathtub, private lounge, and premium bar.",
    price: 3800, capacity: 2, floor: 2, size: 450,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "55\" TV", "Jacuzzi", "Mini Bar", "Balcony"],
    bg: "ri-deluxe",
    image: "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=600&q=80",
    badge: "FEATURED"
  },
  {
    id: 5, number: "203", type: "Family",
    name: "Family Suite",
    desc: "Two bedrooms, a living area and kitchenette — perfect for families.",
    price: 4500, capacity: 4, floor: 2, size: 600,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "2 Smart TVs", "Kitchenette", "Safe"],
    bg: "ri-family",
    image: "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80",
    badge: "AVAILABLE"
  },
  {
    id: 6, number: "301", type: "Suite",
    name: "Executive Suite",
    desc: "Private balcony, Jacuzzi, butler service and breathtaking city panorama.",
    price: 7800, capacity: 2, floor: 3, size: 800,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "65\" TV", "Jacuzzi", "Butler", "Balcony", "Espresso"],
    bg: "ri-suite",
    image: "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=600&q=80",
    badge: "LUXURY"
  },
  {
    id: 7, number: "302", type: "Studio",
    name: "Studio Apartment",
    desc: "Modern studio with kitchenette and workspace, ideal for extended stays.",
    price: 3200, capacity: 2, floor: 3, size: 380,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "Smart TV", "Kitchenette", "Work Desk"],
    bg: "ri-studio",
    image: "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=600&q=80",
    badge: "AVAILABLE"
  },
  {
    id: 8, number: "401", type: "Presidential",
    name: "Presidential Suite",
    desc: "The pinnacle of luxury — private pool, personal butler, panoramic 360° views.",
    price: 25000, capacity: 4, floor: 4, size: 2000,
    status: "AVAILABLE",
    amenities: ["Free WiFi", "AC", "75\" TV", "Private Pool", "Butler", "Bar", "Piano"],
    bg: "ri-presidential",
    image: "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80",
    badge: "EXCLUSIVE"
  }
];

// ── BADGE COLOR MAP ─────────────────────────────────────
const BADGE_STYLES = {
  AVAILABLE:  {bg:"#10b981", color:"#fff"},
  POPULAR:    {bg:"#f59e0b", color:"#1a0a00"},
  FEATURED:   {bg:"#6366f1", color:"#fff"},
  LUXURY:     {bg:"#c9a84c", color:"#1a0a00"},
  EXCLUSIVE:  {bg:"#1a1a2e", color:"#c9a84c"},
  OCCUPIED:   {bg:"#ef4444", color:"#fff"}
};

// ── RENDER A SINGLE CARD ────────────────────────────────
function createRoomCard(room, showBook = true) {
  const b = BADGE_STYLES[room.badge] || BADGE_STYLES.AVAILABLE;
  const amenityTags = room.amenities.slice(0, 4)
    .map(a => `<span class="amenity-tag">${a}</span>`).join('');
  const extraAmenities = room.amenities.length > 4
    ? `<span class="amenity-tag">+${room.amenities.length - 4} more</span>` : '';
  const priceFormatted = room.price >= 1000
    ? '₹' + room.price.toLocaleString('en-IN')
    : '₹' + room.price;

  // इथे आपण चेक करत आहोत की इमेज लिंक आहे की नाही, त्यानुसार HTML स्ट्रक्चर बदलेल
  const imageHTML = room.image 
    ? `<img src="${room.image}" alt="${room.name}" style="width:100%; height:100%; object-fit:cover;">`
    : `<div class="room-img-inner ${room.bg}">
        <div class="window" style="left:30px"></div>
        <div class="window" style="right:30px"></div>
       </div>`;

  return `
    <div class="room-card" onclick="openRoomDetail(${room.id})">
      <div class="room-img" style="height: 220px; overflow: hidden; position: relative;">
        ${imageHTML}
        <div class="room-badge" style="background:${b.bg};color:${b.color}; position: absolute; top: 12px; left: 12px; z-index: 2;">
          ${room.badge}
        </div>
      </div>
      <div class="room-body">
        <div class="room-type-tag">${room.type} · Floor ${room.floor} · ${room.size} sq ft</div>
        <h3>${room.name}</h3>
        <p>${room.desc}</p>
        <div class="room-amenities">
          ${amenityTags}${extraAmenities}
        </div>
        <div class="room-footer">
          <div class="room-price">
            <div class="price-num">${priceFormatted}</div>
            <div class="price-per">per night</div>
          </div>
          ${showBook ? `<button class="btn-book" onclick="event.stopPropagation();handleBook(${room.id})">Book Now</button>` : ''}
        </div>
      </div>
    </div>`;
}

// ── HOMEPAGE: render first 3 rooms ─────────────────────
function renderHomeRooms() {
  const grid = document.getElementById('roomsGrid');
  if (!grid) return;
  const featured = ROOMS.filter(r =>
    ['Presidential','Suite','Deluxe'].includes(r.type));
  const show = [...featured, ...ROOMS].slice(0, 3);
  grid.innerHTML = show.map(r => createRoomCard(r)).join('');
}

// ── ROOMS PAGE: render all / filtered ──────────────────
function renderAllRooms(filtered) {
  const grid = document.getElementById('roomsPageGrid');
  if (!grid) return;
  const list = filtered || ROOMS;
  if (list.length === 0) {
    grid.innerHTML = `<div style="grid-column:1/-1;text-align:center;padding:60px;color:#6b7280">
      <div style="font-size:3rem;margin-bottom:16px">🔍</div>
      <p>No rooms found matching your criteria. Try adjusting the filters.</p>
    </div>`;
    return;
  }
  grid.innerHTML = list.map(r => createRoomCard(r)).join('');
}

// ── FILTER ROOMS ────────────────────────────────────────
function filterRooms() {
  const type    = (document.getElementById('filterType')?.value || '').toLowerCase();
  const maxP    = parseInt(document.getElementById('filterPrice')?.value) || 99999;
  const minCap  = parseInt(document.getElementById('filterCap')?.value) || 1;
  const search  = (document.getElementById('filterSearch')?.value || '').toLowerCase();

  const filtered = ROOMS.filter(r => {
    const matchType = !type || r.type.toLowerCase() === type;
    const matchP    = r.price <= maxP;
    const matchCap  = r.capacity >= minCap;
    const matchS    = !search ||
      r.name.toLowerCase().includes(search) ||
      r.type.toLowerCase().includes(search) ||
      r.amenities.some(a => a.toLowerCase().includes(search));
    return matchType && matchP && matchCap && matchS;
  });

  renderAllRooms(filtered);
  const count = document.getElementById('roomCount');
  if (count) count.textContent = `${filtered.length} room${filtered.length !== 1 ? 's' : ''} found`;
}

// ── OPEN ROOM DETAIL MODAL ──────────────────────────────
function openRoomDetail(id) {
  const room = ROOMS.find(r => r.id === id);
  if (!room) return;

  const allAmenities = room.amenities
    .map(a => `<span class="amenity-tag" style="font-size:.82rem;padding:5px 12px">${a}</span>`)
    .join('');

  const b = BADGE_STYLES[room.badge] || BADGE_STYLES.AVAILABLE;
  const priceFormatted = '₹' + room.price.toLocaleString('en-IN');

  // Modal मधील इमेजसाठी देखील कस्टमायझेशन
  const modalImageHTML = room.image
    ? `<img src="${room.image}" alt="${room.name}" style="width:100%; height:100%; object-fit:cover;">`
    : `<div class="room-img-inner ${room.bg}" style="height:100%">
        <div class="window" style="left:60px;width:55px;height:70px;top:25px"></div>
        <div class="window" style="right:60px;width:55px;height:70px;top:25px"></div>
        <div class="window" style="left:50%;transform:translateX(-50%);width:55px;height:70px;top:25px"></div>
       </div>`;

  document.getElementById('modalBody').innerHTML = `
    <div class="room-img" style="height:260px;margin:-40px -40px 24px;border-radius:24px 24px 0 0;overflow:hidden;position:relative;">
      ${modalImageHTML}
      <div class="room-badge" style="background:${b.bg};color:${b.color};position:absolute;top:16px;left:16px;z-index:2;">
        ${room.badge}
      </div>
    </div>
    <div class="room-type-tag">${room.type} · Room ${room.number} · Floor ${room.floor} · ${room.size} sq ft · Up to ${room.capacity} guest${room.capacity > 1 ? 's' : ''}</div>
    <h3 style="font-size:1.4rem;font-weight:700;margin:6px 0 12px;color:#1a1a2e">${room.name}</h3>
    <p style="color:#6b7280;line-height:1.7;margin-bottom:24px">${room.desc}</p>
    <div style="margin-bottom:24px">
      <h5 style="font-size:.82rem;font-weight:600;text-transform:uppercase;letter-spacing:.5px;color:#374151;margin-bottom:12px">Room Amenities</h5>
      <div style="display:flex;flex-wrap:wrap;gap:8px">${allAmenities}</div>
    </div>
    <div style="display:flex;align-items:center;justify-content:space-between;padding-top:20px;border-top:1px solid #e5e7eb">
      <div>
        <div style="font-size:1.6rem;font-weight:700;color:#1a1a2e">${priceFormatted}</div>
        <div style="font-size:.78rem;color:#6b7280">per night · including taxes</div>
      </div>
      <button class="btn-book" style="padding:12px 28px;font-size:.9rem"
        onclick="handleBook(${room.id});closeModal()">
        Book This Room
      </button>
    </div>`;

  openModal();
}

// ── BOOK ROOM ───────────────────────────────────────────
function handleBook(id) {
  const token = localStorage.getItem('token');
  const room  = ROOMS.find(r => r.id === id);
  if (!room) return;

  if (!token) {
    showToast('Please sign in to book a room', 'error');
    setTimeout(() => window.location.href = '../pages/login.html', 1200);
    return;
  }
  window.location.href = `booking.html?roomId=${id}`;
}