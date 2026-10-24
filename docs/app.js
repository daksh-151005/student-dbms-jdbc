/**
 * Student DBMS Portal - Frontend JavaScript
 * Handles CRUD operations via REST API connected to Java JDBC backend.
 */

let studentsData = [];
let pendingDropRollNo = null;

// Initial sample records used for live GitHub Pages demo mode
const DEFAULT_STUDENTS = [
    { rollNo: "CS101", name: "Aarav Sharma", email: "aarav.sharma@college.edu", department: "Computer Science", semester: 5, gpa: 8.85 },
    { rollNo: "CS102", name: "Priya Patel", email: "priya.patel@college.edu", department: "Information Technology", semester: 5, gpa: 9.20 },
    { rollNo: "EC201", name: "Rohan Gupta", email: "rohan.gupta@college.edu", department: "Electronics", semester: 3, gpa: 7.90 },
    { rollNo: "ME301", name: "Ananya Verma", email: "ananya.verma@college.edu", department: "Mechanical", semester: 7, gpa: 8.45 },
    { rollNo: "CE401", name: "Vikram Malhotra", email: "vikram.m@college.edu", department: "Civil", semester: 4, gpa: 7.60 }
];

function getLocalStudents() {
    const raw = localStorage.getItem('student_db_records');
    if (!raw) {
        localStorage.setItem('student_db_records', JSON.stringify(DEFAULT_STUDENTS));
        return DEFAULT_STUDENTS;
    }
    try { return JSON.parse(raw); } catch (e) { return DEFAULT_STUDENTS; }
}

function saveLocalStudents(list) {
    localStorage.setItem('student_db_records', JSON.stringify(list));
}

// DOM Elements
const studentTableBody = document.getElementById('student-table-body');
const emptyState = document.getElementById('empty-state');
const searchInput = document.getElementById('search-input');
const deptFilter = document.getElementById('dept-filter');
const btnRefresh = document.getElementById('btn-refresh');

// Modals
const studentModal = document.getElementById('student-modal');
const studentForm = document.getElementById('student-form');
const modalTitle = document.getElementById('modal-title');
const formMode = document.getElementById('form-mode');
const formRollNo = document.getElementById('form-roll-no');
const formName = document.getElementById('form-name');
const formEmail = document.getElementById('form-email');
const formDept = document.getElementById('form-dept');
const formSem = document.getElementById('form-sem');
const formGpa = document.getElementById('form-gpa');
const rollHelp = document.getElementById('roll-help');

const dropModal = document.getElementById('drop-modal');
const dropStudentName = document.getElementById('drop-student-name');
const dropStudentRoll = document.getElementById('drop-student-roll');
const dropSqlRoll = document.getElementById('drop-sql-roll');
const dropConfirmBtn = document.getElementById('drop-confirm-btn');

const dbStatusBadge = document.getElementById('db-status-badge');
const dbStatusText = document.getElementById('db-status-text');
const bannerNotice = document.getElementById('banner-notice');
const bannerMessage = document.getElementById('banner-message');

// Stats Elements
const statTotal = document.getElementById('stat-total');
const statAvgGpa = document.getElementById('stat-avg-gpa');
const statDeptCount = document.getElementById('stat-dept-count');

// ==========================================
// Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    fetchDbStatus();
    fetchStudents();
    setupEventListeners();
});

function setupEventListeners() {
    // Search & Filter
    searchInput.addEventListener('input', renderTable);
    deptFilter.addEventListener('change', renderTable);
    btnRefresh.addEventListener('click', () => {
        fetchDbStatus();
        fetchStudents();
        showToast('Refreshed data from database.', 'info');
    });

    // Add Student Modal Open
    document.getElementById('btn-open-add-modal').addEventListener('click', openAddModal);

    // Modal Close buttons
    document.getElementById('modal-close-btn').addEventListener('click', closeStudentModal);
    document.getElementById('modal-cancel-btn').addEventListener('click', closeStudentModal);
    document.getElementById('drop-modal-close-btn').addEventListener('click', closeDropModal);
    document.getElementById('drop-cancel-btn').addEventListener('click', closeDropModal);

    // Backdrop click close
    studentModal.addEventListener('click', (e) => {
        if (e.target === studentModal) closeStudentModal();
    });
    dropModal.addEventListener('click', (e) => {
        if (e.target === dropModal) closeDropModal();
    });

    // Form Submit (Add or Update)
    studentForm.addEventListener('submit', handleFormSubmit);

    // Drop Confirm
    dropConfirmBtn.addEventListener('click', handleDropConfirm);
}

// ==========================================
// API Operations
// ==========================================

// Check Database Status
async function fetchDbStatus() {
    try {
        const res = await fetch('/api/status');
        if (!res.ok) throw new Error('Status endpoint failed');
        const data = await res.json();

        if (data.connected) {
            dbStatusBadge.className = 'status-badge online';
            dbStatusText.textContent = 'Oracle Database Online';
            bannerNotice.classList.add('hidden');
        } else {
            dbStatusBadge.className = 'status-badge offline';
            dbStatusText.textContent = 'Oracle DB Offline';
            bannerNotice.classList.remove('hidden');
            bannerMessage.textContent = 'Notice: Oracle Database is not reachable. Running in interactive preview mode.';
        }
    } catch (err) {
        // When running on GitHub Pages (static hosting)
        dbStatusBadge.className = 'status-badge online';
        dbStatusText.textContent = 'GitHub Live Demo Mode';
        bannerNotice.classList.remove('hidden');
        bannerMessage.textContent = 'Hosting: GitHub Pages (Client Mode). Data is stored interactively in browser storage.';
    }
}

// Fetch all students (SELECT)
async function fetchStudents() {
    try {
        const res = await fetch('/api/students');
        if (!res.ok) throw new Error('Failed to retrieve students');
        const data = await res.json();
        studentsData = data.students || [];
    } catch (err) {
        // Fallback to local storage (GitHub Pages or static host)
        studentsData = getLocalStudents();
    }
    updateStats();
    renderTable();
}

// Add Student (INSERT) or Update Student (UPDATE)
async function handleFormSubmit(e) {
    e.preventDefault();

    const mode = formMode.value;
    const student = {
        rollNo: formRollNo.value.trim(),
        name: formName.value.trim(),
        email: formEmail.value.trim(),
        department: formDept.value.trim(),
        semester: parseInt(formSem.value, 10),
        gpa: parseFloat(formGpa.value)
    };

    const endpoint = mode === 'add' ? '/api/students/add' : '/api/students/update';
    const submitBtn = document.getElementById('modal-submit-btn');
    submitBtn.disabled = true;

    try {
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student)
        });

        const result = await res.json();
        if (res.ok && result.success) {
            showToast(result.message || (mode === 'add' ? 'Student added!' : 'Student updated!'), 'success');
            closeStudentModal();
            fetchStudents();
        } else {
            showToast(result.error || 'Operation failed', 'error');
        }
    } catch (err) {
        // Client-side fallback for GitHub Pages
        if (mode === 'add') {
            const exists = studentsData.some(s => s.rollNo.toLowerCase() === student.rollNo.toLowerCase());
            if (exists) {
                showToast(`Student ${student.rollNo} already exists!`, 'error');
                submitBtn.disabled = false;
                return;
            }
            studentsData.push(student);
            saveLocalStudents(studentsData);
            showToast(`Student ${student.rollNo} added successfully!`, 'success');
        } else {
            const idx = studentsData.findIndex(s => s.rollNo.toLowerCase() === student.rollNo.toLowerCase());
            if (idx !== -1) {
                studentsData[idx] = student;
                saveLocalStudents(studentsData);
                showToast(`Student ${student.rollNo} updated successfully!`, 'success');
            }
        }
        closeStudentModal();
        updateStats();
        renderTable();
    } finally {
        submitBtn.disabled = false;
    }
}

// Drop Student (DELETE)
async function handleDropConfirm() {
    if (!pendingDropRollNo) return;

    dropConfirmBtn.disabled = true;
    try {
        const res = await fetch('/api/students/drop', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ rollNo: pendingDropRollNo })
        });

        const result = await res.json();
        if (res.ok && result.success) {
            showToast(result.message || 'Student dropped successfully!', 'success');
            closeDropModal();
            fetchStudents();
        } else {
            showToast(result.error || 'Failed to drop student', 'error');
        }
    } catch (err) {
        // Client-side fallback for GitHub Pages
        studentsData = studentsData.filter(s => s.rollNo !== pendingDropRollNo);
        saveLocalStudents(studentsData);
        showToast(`Student ${pendingDropRollNo} dropped successfully!`, 'success');
        closeDropModal();
        updateStats();
        renderTable();
    } finally {
        dropConfirmBtn.disabled = false;
    }
}

// ==========================================
// UI Rendering
// ==========================================

function renderTable() {
    const query = searchInput.value.toLowerCase().trim();
    const dept = deptFilter.value;

    const filtered = studentsData.filter(s => {
        const matchQuery = !query ||
            s.rollNo.toLowerCase().includes(query) ||
            s.name.toLowerCase().includes(query) ||
            s.email.toLowerCase().includes(query);
        const matchDept = !dept || s.department === dept;
        return matchQuery && matchDept;
    });

    if (filtered.length === 0) {
        studentTableBody.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    studentTableBody.innerHTML = filtered.map(s => {
        const gpaClass = s.gpa >= 8.5 ? 'high' : (s.gpa >= 7.0 ? 'medium' : 'low');
        return `
            <tr>
                <td><span class="roll-badge">${escapeHtml(s.rollNo)}</span></td>
                <td><strong>${escapeHtml(s.name)}</strong></td>
                <td><span class="text-muted font-mono">${escapeHtml(s.email)}</span></td>
                <td><span class="dept-badge">${escapeHtml(s.department)}</span></td>
                <td>Sem ${s.semester}</td>
                <td><span class="gpa-pill ${gpaClass}">${s.gpa.toFixed(2)}</span></td>
                <td class="text-right">
                    <button class="btn-icon edit" title="Update Student Entry" onclick="openEditModal('${escapeHtml(s.rollNo)}')">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                        </svg>
                    </button>
                    <button class="btn-icon drop" title="Drop Student Entry" onclick="openDropModal('${escapeHtml(s.rollNo)}')">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            <line x1="10" y1="11" x2="10" y2="17"></line>
                            <line x1="14" y1="11" x2="14" y2="17"></line>
                        </svg>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function updateStats() {
    statTotal.textContent = studentsData.length;

    if (studentsData.length === 0) {
        statAvgGpa.textContent = '0.00';
        statDeptCount.textContent = '0';
        return;
    }

    const totalGpa = studentsData.reduce((acc, s) => acc + (s.gpa || 0), 0);
    const avgGpa = totalGpa / studentsData.length;
    statAvgGpa.textContent = avgGpa.toFixed(2);

    const depts = new Set(studentsData.map(s => s.department));
    statDeptCount.textContent = depts.size;
}

// ==========================================
// Modal Helpers
// ==========================================

function openAddModal() {
    formMode.value = 'add';
    modalTitle.textContent = 'Add New Student Entry';
    formRollNo.disabled = false;
    rollHelp.textContent = 'Primary key in database. Cannot be altered once inserted.';
    document.getElementById('modal-submit-btn').firstElementChild.textContent = 'Save Student';

    studentForm.reset();
    formSem.value = 1;
    formGpa.value = '8.00';
    studentModal.classList.remove('hidden');
    formRollNo.focus();
}

function openEditModal(rollNo) {
    const student = studentsData.find(s => s.rollNo === rollNo);
    if (!student) return;

    formMode.value = 'update';
    modalTitle.textContent = 'Update Student Information';
    formRollNo.value = student.rollNo;
    formRollNo.disabled = true;
    rollHelp.textContent = 'Roll Number (Primary Key) cannot be modified.';
    document.getElementById('modal-submit-btn').firstElementChild.textContent = 'Update Changes';

    formName.value = student.name;
    formEmail.value = student.email;
    formDept.value = student.department;
    formSem.value = student.semester;
    formGpa.value = student.gpa.toFixed(2);

    studentModal.classList.remove('hidden');
    formName.focus();
}

function closeStudentModal() {
    studentModal.classList.add('hidden');
}

function openDropModal(rollNo) {
    const student = studentsData.find(s => s.rollNo === rollNo);
    if (!student) return;

    pendingDropRollNo = rollNo;
    dropStudentName.textContent = student.name;
    dropStudentRoll.textContent = student.rollNo;
    dropSqlRoll.textContent = student.rollNo;
    dropModal.classList.remove('hidden');
}

function closeDropModal() {
    dropModal.classList.add('hidden');
    pendingDropRollNo = null;
}

// Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';

    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
}
