// --- UPDATED FOR PRODUCTION ---
const API_BASE = 'https://pg-management-j9ai.onrender.com/api';

// For local development in the future, you can swap it back to:
// const API_BASE = 'http://localhost:8080/api';
// ------------------------------

// --- State Management ---
let token = localStorage.getItem('jwt_token');
let currentUsername = localStorage.getItem('username');
let alertDaysSetting = localStorage.getItem('alertDays') || 3;

let currentPage = 0;
let pageSize = 10;
let sortBy = 'id';
let sortAs = 'ASC';
let currentSearch = '';

let customersList = [];
let roomsList = [];
let roomSearch = '';
let roomFilter = 'ALL';
let customerRoomIsAllocated = false;
let customerRoomOriginalValue = null;
let roomAvailabilityLoaded = false;
let roomOccupancyLoaded = false;

// --- DOM Elements ---
const views = {
    auth: document.getElementById('auth-view'),
    app: document.getElementById('app-view')
};

const pages = {
    customers: document.getElementById('customers-page'),
    rooms: document.getElementById('rooms-page'),
    settings: document.getElementById('settings-page')
};

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    checkAuthState();
    setupEventListeners();
});

// --- Utility Functions ---

function showToast(message, title = 'Notification', type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
    `;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('hiding');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function showConfirmationToast(message, title = 'Confirm Change') {
    return new Promise((resolve) => {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = 'toast warning confirmation-toast';

        const titleEl = document.createElement('div');
        titleEl.className = 'toast-title';
        titleEl.textContent = title;

        const messageEl = document.createElement('div');
        messageEl.className = 'toast-message';
        messageEl.textContent = message;

        const actionsEl = document.createElement('div');
        actionsEl.className = 'toast-actions';

        const cancelButton = document.createElement('button');
        cancelButton.type = 'button';
        cancelButton.className = 'toast-btn secondary';
        cancelButton.textContent = 'Cancel';

        const confirmButton = document.createElement('button');
        confirmButton.type = 'button';
        confirmButton.className = 'toast-btn primary';
        confirmButton.textContent = 'Save';

        const close = (confirmed) => {
            toast.classList.add('hiding');
            setTimeout(() => toast.remove(), 300);
            resolve(confirmed);
        };

        cancelButton.addEventListener('click', () => close(false));
        confirmButton.addEventListener('click', () => close(true));

        actionsEl.append(cancelButton, confirmButton);
        toast.append(titleEl, messageEl, actionsEl);
        container.appendChild(toast);
    });
}

async function handleInlineCustomerEdit(e) {
    const target = e.target;
    const id = target.getAttribute('data-id');
    const field = target.getAttribute('data-field');
    const value = target.value;
    const previousValue = target.dataset.originalValue ?? '';

    if (value === previousValue) {
        return;
    }

    const confirmed = await showConfirmationToast('Save this inline edit?', 'Confirm Save');
    if (!confirmed) {
        target.value = previousValue;
        return;
    }

    const payload = {};
    payload[field] = target.type === 'number' ? parseFloat(value) : value;

    try {
        await fetchWithAuth(`/customers/${id}`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        target.dataset.originalValue = value;
        showToast('Updated successfully', 'Success', 'success');
    } catch (err) {
        target.value = previousValue;
    }
}

function attachInlineEditListeners() {
    document.querySelectorAll('.inline-edit').forEach(el => {
        el.dataset.originalValue = el.value;
        el.addEventListener('change', handleInlineCustomerEdit);
    });
}

function handleApiError(error) {
    console.error('API Error:', error);
    if (error.status === 401 || error.status === 403) {
        logout();
        showToast('Session expired. Please log in again.', 'Error', 'error');
    } else {
        showToast(error.message || 'An unexpected error occurred.', error.title || 'Error', 'error');
    }
}

async function fetchWithAuth(url, options = {}) {
    if (!token) throw new Error('No token found');
    
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers
    };
    
    try {
        const response = await fetch(`${API_BASE}${url}`, { ...options, headers });
        
        if (!response.ok) {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (e) {}
            
            throw {
                status: response.status,
                message: errorData.message || errorData.details || 'Request failed',
                title: errorData.details || 'Error'
            };
        }
        
        // Handle empty responses
        const text = await response.text();
        return text ? JSON.parse(text) : {};
    } catch (error) {
        handleApiError(error);
        throw error;
    }
}

// --- Authentication ---

function checkAuthState() {
    if (token) {
        views.auth.classList.remove('active');
        views.app.classList.add('active');
        document.getElementById('current-user-name').innerText = currentUsername;
        document.getElementById('settings-username').value = currentUsername;
        document.getElementById('alert-days').value = alertDaysSetting;
        loadCustomers();
    } else {
        views.app.classList.remove('active');
        views.auth.classList.add('active');
    }
}

function formatRoomStatus(status) {
    return status === 'RESERVED' ? 'Reserved' : 'Unreserved';
}

function getRoomBadgeClass(status) {
    return status === 'RESERVED' ? 'status-pending' : 'status-active';
}

function getPageTitle(pageId) {
    if (pageId === 'customers-page') return 'Customers';
    if (pageId === 'rooms-page') return 'Rooms';
    return 'Settings';
}

function setRoomSyncStatus(message, type = '') {
    const status = document.getElementById('rooms-sync-status');
    if (!status) {
        return;
    }

    status.textContent = message;
    status.className = type ? `field-hint ${type}` : 'field-hint';
}

function getRoomOccupiedBeds(room) {
    return Number(room?.occupiedBeds ?? 0);
}

function getRoomAvailableBeds(room) {
    const totalBeds = Number(room?.noOfBed ?? 0);
    const occupiedBeds = getRoomOccupiedBeds(room);
    return Math.max(totalBeds - occupiedBeds, 0);
}

function getDerivedRoomStatus(room) {
    return getRoomAvailableBeds(room) > 0 ? 'UNRESERVED' : 'RESERVED';
}

async function fetchAllCustomersForRoomLookup() {
    const pageSize = 200;
    const customers = [];

    for (let page = 0; page < 50; page++) {
        const batch = await fetchWithAuth(`/customers?page=${page}&size=${pageSize}&sortBy=id&sortAs=ASC`);
        const list = Array.isArray(batch) ? batch : [];
        customers.push(...list);

        if (list.length < pageSize) {
            break;
        }
    }

    return customers;
}

async function login(username, password) {
    try {
        const response = await fetch(`${API_BASE}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userName: username.trim(), userPassword: password })
        });
        
        if (response.ok) {
            let rawToken = await response.text();
            token = rawToken.replace(/^"|"$/g, '').trim();
            if (token === "User Verification Unsuccessful" || token.includes("Unsuccessful") || token === "Fail") {
                showToast('Invalid credentials', 'Login Failed', 'error');
                return;
            }
            currentUsername = username.trim();
            localStorage.setItem('jwt_token', token);
            localStorage.setItem('username', currentUsername);
            showToast('Logged in successfully', 'Welcome', 'success');
            checkAuthState();
        } else {
            let errorData = {};
            try { errorData = await response.json(); } catch(e){}
            showToast(errorData.message || 'Invalid credentials', 'Login Failed', 'error');
        }
    } catch (e) {
        showToast('Server connection failed', 'Error', 'error');
    }
}

async function register(username, password) {
    try {
        const response = await fetch(`${API_BASE}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userName: username.trim(), userPassword: password })
        });
        
        if (response.ok) {
            showToast('Account created successfully. Please login.', 'Success', 'success');
            document.querySelector('.tab-btn[data-target="login-form"]').click();
            document.getElementById('login-username').value = username.trim();
            document.getElementById('login-password').value = '';
        } else {
            let errorData = {};
            try { errorData = await response.json(); } catch(e){}
            showToast(errorData.message || 'Registration failed', 'Error', 'error');
        }
    } catch (e) {
        showToast('Server connection failed', 'Error', 'error');
    }
}

function logout() {
    token = null;
    currentUsername = null;
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    checkAuthState();
}

// --- Room Management ---

async function loadRooms() {
    try {
        setRoomSyncStatus('Loading room status from the backend...');

        const [reservedResult, unreservedResult, customersResult] = await Promise.allSettled([
            fetchWithAuth('/rooms?status=RESERVED'),
            fetchWithAuth('/rooms?status=UNRESERVED'),
            fetchAllCustomersForRoomLookup()
        ]);

        const reservedRooms = reservedResult.status === 'fulfilled' && Array.isArray(reservedResult.value) ? reservedResult.value : [];
        const unreservedRooms = unreservedResult.status === 'fulfilled' && Array.isArray(unreservedResult.value) ? unreservedResult.value : [];
        const lookupCustomers = customersResult.status === 'fulfilled' && Array.isArray(customersResult.value) ? customersResult.value : customersList;
        const occupancyMap = new Map();

        lookupCustomers.forEach(customer => {
            const roomNo = Number(customer.roomNo);
            if (!Number.isNaN(roomNo) && roomNo > 0) {
                occupancyMap.set(roomNo, (occupancyMap.get(roomNo) || 0) + 1);
            }
        });

        roomsList = [...reservedRooms, ...unreservedRooms].map(room => ({
            ...room,
            occupiedBeds: occupancyMap.get(Number(room.roomNo)) || 0,
            availableBeds: getRoomAvailableBeds({
                ...room,
                occupiedBeds: occupancyMap.get(Number(room.roomNo)) || 0
            }),
            calculatedStatus: getDerivedRoomStatus({
                ...room,
                occupiedBeds: occupancyMap.get(Number(room.roomNo)) || 0
            })
        }));
        roomAvailabilityLoaded = true;
        roomOccupancyLoaded = customersResult.status === 'fulfilled';
        renderRoomsTable();
        if (document.getElementById('cust-room-no')?.value) {
            updateCustomerRoomAvailability(document.getElementById('cust-room-no').value);
        }

        if (reservedResult.status === 'rejected' || unreservedResult.status === 'rejected') {
            setRoomSyncStatus('Some room data could not be loaded from the backend.', 'warning');
        } else if (!roomOccupancyLoaded) {
            setRoomSyncStatus('Room list loaded, but bed counts could not be calculated right now.', 'warning');
        } else {
            setRoomSyncStatus('Room status loaded from the backend.', 'success');
        }
    } catch (e) {
        roomAvailabilityLoaded = false;
        roomOccupancyLoaded = false;
        setRoomSyncStatus('Room status could not be loaded.', 'danger');
    }
}

function renderRoomsTable() {
    const tbody = document.getElementById('rooms-tbody');
    if (!tbody) {
        return;
    }

    const searchTerm = roomSearch.trim().toLowerCase();
    const filteredRooms = roomsList
        .filter(room => roomFilter === 'ALL' || (room.calculatedStatus || room.roomStatus) === roomFilter)
        .filter(room => {
            if (!searchTerm) return true;
            return String(room.roomNo).includes(searchTerm);
        })
        .sort((a, b) => Number(a.roomNo) - Number(b.roomNo));

    tbody.innerHTML = '';

    filteredRooms.forEach(room => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${room.roomNo ?? ''}</td>
            <td>${getRoomOccupiedBeds(room)} / ${room.noOfBed ?? ''}</td>
            <td>${getRoomAvailableBeds(room)}</td>
            <td><span class="status-badge ${getRoomBadgeClass(room.calculatedStatus || room.roomStatus)}">${formatRoomStatus(room.calculatedStatus || room.roomStatus)}</span></td>
        `;
        tbody.appendChild(tr);
    });

    document.getElementById('rooms-total-count').innerText = roomsList.length;
    document.getElementById('rooms-reserved-count').innerText = roomsList.filter(room => (room.calculatedStatus || room.roomStatus) === 'RESERVED').length;
    document.getElementById('rooms-unreserved-count').innerText = roomsList.filter(room => (room.calculatedStatus || room.roomStatus) === 'UNRESERVED').length;
}

function updateCustomerRoomAvailability(roomNo) {
    const hint = document.getElementById('cust-room-status-hint');
    if (!hint) {
        return;
    }

    if (!roomNo) {
        customerRoomIsAllocated = false;
        hint.textContent = 'Enter a room number to check availability.';
        hint.className = 'field-hint';
        return;
    }

    const normalizedRoomNo = Number(roomNo);
    const matchingRoom = roomsList.find(room => Number(room.roomNo) === normalizedRoomNo);
    const isSameRoomAsOriginal = normalizedRoomNo === Number(customerRoomOriginalValue);
    const availableBeds = matchingRoom ? getRoomAvailableBeds(matchingRoom) : 0;

    if (!matchingRoom) {
        customerRoomIsAllocated = false;
        hint.textContent = roomAvailabilityLoaded
            ? 'Room not found. Add it in Rooms first.'
            : 'Room status will be checked when you save.';
        hint.className = 'field-hint warning';
        return;
    }

    if (!roomOccupancyLoaded) {
        customerRoomIsAllocated = false;
        hint.textContent = 'Checking room availability...';
        hint.className = 'field-hint warning';
        return;
    }

    if (availableBeds > 0 || (matchingRoom.calculatedStatus || matchingRoom.roomStatus) === 'UNRESERVED' || isSameRoomAsOriginal) {
        customerRoomIsAllocated = false;
        hint.textContent = isSameRoomAsOriginal && availableBeds === 0 ? 'Current room selected' : 'Room available';
        hint.className = isSameRoomAsOriginal && availableBeds === 0 ? 'field-hint warning' : 'field-hint success';
        return;
    }

    customerRoomIsAllocated = true;
    hint.textContent = 'Room unreserved';
    hint.className = 'field-hint danger';
}

async function saveRoom(roomData) {
    try {
        await fetchWithAuth('/rooms', {
            method: 'POST',
            body: JSON.stringify(roomData)
        });
        showToast('Room saved successfully', 'Success', 'success');
        closeModal('room-modal');
        document.getElementById('room-form').reset();
        loadRooms();
    } catch (e) {
        // handled
    }
}

// --- Customer Management ---

async function loadCustomers() {
    try {
        let data;
        if (currentSearch) {
            data = await fetchWithAuth(`/customers/search?keyword=${encodeURIComponent(currentSearch)}&page=${currentPage}&size=${pageSize}&sortBy=${sortBy}&sortAs=${sortAs}`);
        } else {
            data = await fetchWithAuth(`/customers?page=${currentPage}&size=${pageSize}&sortBy=${sortBy}&sortAs=${sortAs}`);
        }
        
        customersList = data || [];
        renderCustomersTable();
        document.getElementById('page-indicator').innerText = `Page ${currentPage + 1}`;
        document.getElementById('prev-page').disabled = currentPage === 0;
        document.getElementById('next-page').disabled = customersList.length < pageSize;
        
    } catch (e) {
        // Error handled in fetchWithAuth
    }
}

async function deleteCustomer(id) {
    if(confirm('Are you sure you want to delete this customer?')) {
        try {
            await fetchWithAuth(`/customers/${id}`, { method: 'DELETE' });
            showToast('Customer deleted', 'Success', 'success');
            loadCustomers();
        } catch (e) { }
    }
}

function openCustomerModal(id) {
    const cust = customersList.find(c => c.id === id);
    if (!cust) return;

    customerRoomOriginalValue = cust.roomNo ?? null;
    document.getElementById('customer-id').value = cust.id;
    document.getElementById('customer-modal-title').innerText = 'Edit Customer';

    document.getElementById('cust-name').value = cust.name || '';
    document.getElementById('cust-phone').value = cust.phoneNo || '';
    document.getElementById('cust-join-date').value = cust.joinDate || '';
    document.getElementById('cust-room-no').value = cust.roomNo || '';
    document.getElementById('cust-leave-date').value = cust.leaveDate || '';
    document.getElementById('cust-advance').value = cust.advanceAmount || 0;
    document.getElementById('cust-rent').value = cust.rentFee || 0;
    document.getElementById('cust-frequency').value = cust.frequencyType || 'MONTH';
    document.getElementById('cust-fee-status').value = cust.feeStatus || 'PENDING';
    document.getElementById('cust-sharing').value = cust.sharing || 'SINGLE';
    document.getElementById('cust-status').value = cust.status || 'ACTIVE';

    const list = document.getElementById('accessories-list');
    list.innerHTML = '';
    if (cust.rentedAccessoriesList) {
        cust.rentedAccessoriesList.forEach(acc => {
            const div = document.createElement('div');
            div.className = 'accessory-item';
            div.innerHTML = `
                <input type="text" placeholder="Name" class="acc-name" value="${acc.accessoriesName}" required>
                <input type="number" step="0.01" placeholder="Price" class="acc-price" value="${acc.accessoriesPrice}" required>
                <button type="button" class="remove-acc-btn" onclick="this.parentElement.remove()">x</button>
            `;
            list.appendChild(div);
        });
    }

    updateCustomerRoomAvailability(cust.roomNo);
    document.getElementById('customer-modal').classList.add('active');
}

function openMessageModal(id) {
    document.getElementById('message-form').reset();
    document.getElementById('message-customer-id').value = id;
    document.getElementById('message-modal').classList.add('active');
}

function openCustomerInfoModal(id) {
    const cust = customersList.find(c => c.id === id);
    if (!cust) {
        return;
    }

    const room = roomsList.find(item => Number(item.roomNo) === Number(cust.roomNo));
    const availableBeds = room ? getRoomAvailableBeds(room) : 0;
    const roomStatusText = room
        ? ((room.calculatedStatus || room.roomStatus) === 'UNRESERVED' ? 'Available' : 'Unreserved')
        : 'Not found';

    const setText = (idName, value) => {
        const element = document.getElementById(idName);
        if (element) {
            element.textContent = value ?? '-';
        }
    };

    setText('info-customer-name', cust.name || '-');
    setText('info-customer-phone', cust.phoneNo || '-');
    setText('info-customer-room-no', cust.roomNo ?? '-');
    setText('info-customer-room-status', roomStatusText);
    setText('info-customer-join-date', cust.joinDate || '-');
    setText('info-customer-leave-date', cust.leaveDate || '-');
    setText('info-customer-status', cust.status || '-');
    setText('info-customer-fee-status', cust.feeStatus || '-');
    setText('info-customer-rent', cust.rentFee ?? '-');
    setText('info-customer-advance', cust.advanceAmount ?? '-');
    setText('info-customer-frequency', cust.frequencyType || '-');
    setText('info-customer-sharing', cust.sharing || '-');

    const accessoriesList = document.getElementById('info-accessories-list');
    if (accessoriesList) {
        const accessories = Array.isArray(cust.rentedAccessoriesList) ? cust.rentedAccessoriesList : [];
        accessoriesList.innerHTML = accessories.length
            ? accessories.map(acc => `
                <div class="detail-item detail-full">
                    <span class="detail-label">${acc.accessoriesName || 'Accessory'}</span>
                    <strong class="detail-value">${acc.accessoriesPrice ?? '-'}</strong>
                </div>
            `).join('')
            : '<div class="empty-state">No rented accessories.</div>';
    }

    document.getElementById('customer-info-modal').classList.add('active');
}

function openRoomModal() {
    document.getElementById('room-form').reset();
    loadRooms();
    document.getElementById('room-modal').classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

function renderCustomersTable() {
    const tbody = document.getElementById('customers-tbody');
    tbody.innerHTML = '';

    customersList.forEach(cust => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${cust.id}</td>
            <td><input type="text" class="inline-edit" data-id="${cust.id}" data-field="name" value="${cust.name || ''}"></td>
            <td><input type="text" class="inline-edit" data-id="${cust.id}" data-field="phoneNo" value="${cust.phoneNo || ''}"></td>
            <td>${cust.roomNo ?? ''}</td>
            <td>${cust.joinDate || ''}</td>
            <td>
                <select class="inline-edit" data-id="${cust.id}" data-field="status">
                    <option value="ACTIVE" ${cust.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="NOTICE_PERIOD" ${cust.status === 'NOTICE_PERIOD' ? 'selected' : ''}>NOTICE_PERIOD</option>
                    <option value="INACTIVE" ${cust.status === 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </td>
            <td>
                <select class="inline-edit" data-id="${cust.id}" data-field="feeStatus">
                    <option value="PAID" ${cust.feeStatus === 'PAID' ? 'selected' : ''}>PAID</option>
                    <option value="PENDING" ${cust.feeStatus === 'PENDING' ? 'selected' : ''}>PENDING</option>
                </select>
            </td>
            <td><input type="number" class="inline-edit" data-id="${cust.id}" data-field="rentFee" value="${cust.rentFee || 0}"></td>
            <td>
                <div class="actions-menu">
                    <button class="action-btn action-trigger" type="button" aria-label="Open actions">&#8942;</button>
                    <div class="actions-dropdown">
                        <button type="button" class="dropdown-item" onclick="openCustomerModal(${cust.id})">Edit</button>
                        <button type="button" class="dropdown-item" onclick="openCustomerInfoModal(${cust.id})">Info</button>
                        <button type="button" class="dropdown-item" onclick="openMessageModal(${cust.id})">Message</button>
                        <button type="button" class="dropdown-item danger" onclick="deleteCustomer(${cust.id})">Delete</button>
                    </div>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });

    attachInlineEditListeners();

    document.querySelectorAll('.action-trigger').forEach(button => {
        button.addEventListener('click', (event) => {
            event.stopPropagation();
            const menu = event.currentTarget.closest('.actions-menu');
            document.querySelectorAll('.actions-menu.open').forEach(openMenu => {
                if (openMenu !== menu) {
                    openMenu.classList.remove('open');
                }
            });
            menu.classList.toggle('open');
        });
    });
}

async function saveCustomer(formData) {
    const isEdit = !!formData.id;
    const url = isEdit ? `/customers/${formData.id}` : `/customers`;
    const method = isEdit ? 'PUT' : 'POST';

    try {
        if (!roomAvailabilityLoaded) {
            try {
                await loadRooms();
            } catch (error) {
                // If room lookup fails, let the backend perform the final validation.
            }
        }

        if (customerRoomIsAllocated) {
            showToast('Room allocated', 'Room Error', 'error');
            return;
        }

        await fetchWithAuth(url, {
            method,
            body: JSON.stringify(formData)
        });
        showToast('Customer saved successfully', 'Success', 'success');
        closeModal('customer-modal');
        loadCustomers();
    } catch (e) {
        // handled
    }
}

function setupEventListeners() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));

            e.target.classList.add('active');
            document.getElementById(e.target.getAttribute('data-target')).classList.add('active');
        });
    });

    document.getElementById('login-form').addEventListener('submit', (e) => {
        e.preventDefault();
        login(document.getElementById('login-username').value, document.getElementById('login-password').value);
    });

    document.getElementById('register-form').addEventListener('submit', (e) => {
        e.preventDefault();
        register(document.getElementById('reg-username').value, document.getElementById('reg-password').value);
    });

    document.getElementById('logout-btn').addEventListener('click', logout);

    document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            document.querySelectorAll('.sidebar-nav .nav-item').forEach(nav => nav.classList.remove('active'));
            document.querySelectorAll('.page-content').forEach(page => page.classList.remove('active'));

            e.currentTarget.classList.add('active');
            const targetPage = e.currentTarget.getAttribute('data-page');
            document.getElementById(targetPage).classList.add('active');

            document.getElementById('page-title').innerText = getPageTitle(targetPage);
            if (targetPage === 'rooms-page') {
                loadRooms();
            }
        });
    });

    let searchTimeout;
    document.getElementById('search-input').addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            currentSearch = e.target.value;
            currentPage = 0;
            loadCustomers();
        }, 500);
    });

    document.getElementById('sort-by-select').addEventListener('change', (e) => {
        sortBy = e.target.value;
        loadCustomers();
    });

    document.getElementById('sort-as-select').addEventListener('change', (e) => {
        sortAs = e.target.value;
        loadCustomers();
    });

    document.getElementById('prev-page').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            loadCustomers();
        }
    });

    document.getElementById('next-page').addEventListener('click', () => {
        currentPage++;
        loadCustomers();
    });

    document.getElementById('update-pending-btn').addEventListener('click', async () => {
        try {
            await fetchWithAuth('/customers/rent-pending');
            showToast('Pending status updated', 'Success', 'success');
            loadCustomers();
        } catch (e) { }
    });

    document.getElementById('trigger-alert-btn').addEventListener('click', async () => {
        try {
            await fetchWithAuth(`/alert/${alertDaysSetting}`);
            showToast(`Alerts sent for ${alertDaysSetting} days before`, 'Success', 'success');
        } catch (e) { }
    });

    document.getElementById('message-all-btn').addEventListener('click', async () => {
        const selectedIds = Array.from(document.querySelectorAll('.row-checkbox:checked'))
            .map(cb => Number(cb.getAttribute('data-id')));
        const selectedCustomers = customersList.filter(customer => selectedIds.includes(Number(customer.id)));

        if (selectedCustomers.length === 0) {
            showToast('Please select at least one customer', 'Warning', 'warning');
            return;
        }

        try {
            const result = await fetchWithAuth('/customers/alert', {
                method: 'POST',
                body: JSON.stringify(selectedCustomers)
            });
            showToast(`Messages sent to ${Array.isArray(result) ? result.length : selectedCustomers.length} customers`, 'Success', 'success');
        } catch (e) { }
    });

    document.getElementById('add-customer-btn').addEventListener('click', () => {
        document.getElementById('customer-form').reset();
        document.getElementById('customer-id').value = '';
        document.getElementById('customer-modal-title').innerText = 'Add Customer';
        document.getElementById('accessories-list').innerHTML = '';
        customerRoomOriginalValue = null;
        customerRoomIsAllocated = false;
        updateCustomerRoomAvailability('');
        loadRooms();
        document.getElementById('customer-modal').classList.add('active');
    });

    document.getElementById('add-room-btn').addEventListener('click', openRoomModal);

    document.getElementById('refresh-rooms-btn').addEventListener('click', loadRooms);

    document.getElementById('room-search-input').addEventListener('input', (e) => {
        roomSearch = e.target.value;
        renderRoomsTable();
    });

    document.getElementById('room-status-select').addEventListener('change', (e) => {
        roomFilter = e.target.value;
        renderRoomsTable();
    });

    document.getElementById('add-accessory-btn').addEventListener('click', () => {
        const list = document.getElementById('accessories-list');
        const div = document.createElement('div');
        div.className = 'accessory-item';
        div.innerHTML = `
            <input type="text" placeholder="Name" class="acc-name" required>
            <input type="number" step="0.01" placeholder="Price" class="acc-price" required>
            <button type="button" class="remove-acc-btn" onclick="this.parentElement.remove()">x</button>
        `;
        list.appendChild(div);
    });

    document.getElementById('cust-room-no').addEventListener('input', (e) => {
        if (!roomAvailabilityLoaded) {
            loadRooms().then(() => updateCustomerRoomAvailability(e.target.value));
            return;
        }
        updateCustomerRoomAvailability(e.target.value);
    });

    document.getElementById('customer-form').addEventListener('submit', (e) => {
        e.preventDefault();

        const accessories = Array.from(document.querySelectorAll('.accessory-item')).map(item => ({
            accessoriesName: item.querySelector('.acc-name').value,
            accessoriesPrice: parseFloat(item.querySelector('.acc-price').value)
        }));

        const formData = {
            name: document.getElementById('cust-name').value,
            phoneNo: document.getElementById('cust-phone').value,
            joinDate: document.getElementById('cust-join-date').value,
            roomNo: parseInt(document.getElementById('cust-room-no').value, 10),
            leaveDate: document.getElementById('cust-leave-date').value || null,
            advanceAmount: parseFloat(document.getElementById('cust-advance').value),
            rentFee: parseFloat(document.getElementById('cust-rent').value),
            frequencyType: document.getElementById('cust-frequency').value,
            feeStatus: document.getElementById('cust-fee-status').value,
            sharing: document.getElementById('cust-sharing').value,
            status: document.getElementById('cust-status').value,
            rentedAccessoriesList: accessories
        };

        const id = document.getElementById('customer-id').value;
        if (id) formData.id = parseInt(id);

        saveCustomer(formData);
    });

    document.getElementById('room-form').addEventListener('submit', (e) => {
        e.preventDefault();

        const roomData = {
            roomNo: parseInt(document.getElementById('room-no').value, 10),
            noOfBed: parseInt(document.getElementById('room-bed-count').value, 10),
            roomStatus: document.getElementById('room-status').value
        };

        saveRoom(roomData);
    });

    document.getElementById('message-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const customerId = document.getElementById('message-customer-id').value;
        const content = document.getElementById('msg-content').value;

        try {
            await fetchWithAuth(`/customers/${customerId}/sms`, {
                method: 'POST',
                body: JSON.stringify({ message: content, status: 'SENT' })
            });
            showToast('Message sent successfully', 'Success', 'success');
            closeModal('message-modal');
        } catch (err) { }
    });

    document.getElementById('system-settings-form').addEventListener('submit', (e) => {
        e.preventDefault();
        alertDaysSetting = document.getElementById('alert-days').value;
        localStorage.setItem('alertDays', alertDaysSetting);
        showToast('Settings saved', 'Success', 'success');
    });

    document.getElementById('change-password-form').addEventListener('submit', (e) => {
        e.preventDefault();
        showToast('Password changed successfully (Mocked)', 'Success', 'success');
        document.getElementById('change-password-form').reset();
    });

    document.addEventListener('click', (event) => {
        if (!event.target.closest('.actions-menu')) {
            document.querySelectorAll('.actions-menu.open').forEach(menu => menu.classList.remove('open'));
        }
    });

    document.querySelectorAll('.close-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
        });
    });
}