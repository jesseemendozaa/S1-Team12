/* ============================================================
   FishFinder NorCal — Client-Side JavaScript
   ============================================================ */

document.addEventListener('DOMContentLoaded', function () {

    /* ----------------------------------------------------------
       Utility: show an error message next to a field
       ---------------------------------------------------------- */
    function showError(fieldId, message) {
        clearFieldError(fieldId);
        var field = document.getElementById(fieldId);
        if (!field) return;
        var err = document.createElement('div');
        err.className = 'form-error';
        err.textContent = message;
        field.parentNode.insertBefore(err, field.nextSibling);
    }

    /* Remove error for a single field */
    function clearFieldError(fieldId) {
        var field = document.getElementById(fieldId);
        if (!field) return;
        var next = field.nextSibling;
        while (next && next.nodeType !== 1) next = next.nextSibling;
        if (next && next.classList && next.classList.contains('form-error')) {
            next.remove();
        }
    }

    /* Remove ALL error displays on the page */
    function clearErrors() {
        var errors = document.querySelectorAll('.form-error');
        errors.forEach(function (el) { el.remove(); });
    }

    /* ----------------------------------------------------------
       Register Form Validation
       ---------------------------------------------------------- */
    var registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            clearErrors();
            var valid = true;

            var username = document.getElementById('username');
            var email    = document.getElementById('email');
            var password = document.getElementById('password');
            var confirm  = document.getElementById('confirmPassword');

            if (username && !username.value.trim()) {
                showError('username', 'Username is required.');
                valid = false;
            }

            if (email) {
                if (!email.value.trim()) {
                    showError('email', 'Email is required.');
                    valid = false;
                } else {
                    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(email.value.trim())) {
                        showError('email', 'Please enter a valid email address.');
                        valid = false;
                    }
                }
            }

            if (password) {
                if (!password.value) {
                    showError('password', 'Password is required.');
                    valid = false;
                } else if (password.value.length < 6) {
                    showError('password', 'Password must be at least 6 characters.');
                    valid = false;
                }
            }

            if (confirm) {
                if (password && confirm.value !== password.value) {
                    showError('confirmPassword', 'Passwords do not match.');
                    valid = false;
                }
            }

            if (!valid) e.preventDefault();
        });
    }

    /* ----------------------------------------------------------
       Login Form Validation
       ---------------------------------------------------------- */
    var loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function (e) {
            clearErrors();
            var valid = true;

            var username = document.getElementById('username');
            var password = document.getElementById('password');

            if (username && !username.value.trim()) {
                showError('username', 'Username is required.');
                valid = false;
            }

            if (password && !password.value) {
                showError('password', 'Password is required.');
                valid = false;
            }

            if (!valid) e.preventDefault();
        });
    }

    /* ----------------------------------------------------------
       Catch Report Form Validation
       ---------------------------------------------------------- */
    var catchForm = document.getElementById('catchReportForm');
    if (catchForm) {
        catchForm.addEventListener('submit', function (e) {
            clearErrors();
            var valid = true;

            var locationId = document.getElementById('locationId');
            var speciesId  = document.getElementById('speciesId');
            var catchDate  = document.getElementById('catchDate');
            var weight     = document.getElementById('weightLbs');
            var length     = document.getElementById('lengthInches');

            if (locationId && !locationId.value) {
                showError('locationId', 'Please select a location.');
                valid = false;
            }

            if (speciesId && !speciesId.value) {
                showError('speciesId', 'Please select a species.');
                valid = false;
            }

            if (catchDate && !catchDate.value) {
                showError('catchDate', 'Catch date is required.');
                valid = false;
            }

            if (weight && weight.value) {
                var w = parseFloat(weight.value);
                if (isNaN(w) || w <= 0) {
                    showError('weightLbs', 'Weight must be a positive number.');
                    valid = false;
                }
            }

            if (length && length.value) {
                var l = parseFloat(length.value);
                if (isNaN(l) || l <= 0) {
                    showError('lengthInches', 'Length must be a positive number.');
                    valid = false;
                }
            }

            if (!valid) e.preventDefault();
        });
    }

    /* ----------------------------------------------------------
       Location Form Validation
       ---------------------------------------------------------- */
    var locationForm = document.getElementById('locationForm');
    if (locationForm) {
        locationForm.addEventListener('submit', function (e) {
            clearErrors();
            var valid = true;

            var name   = document.getElementById('locationName');
            var lat    = document.getElementById('latitude');
            var lng    = document.getElementById('longitude');

            if (name && !name.value.trim()) {
                showError('locationName', 'Location name is required.');
                valid = false;
            }

            if (lat && lat.value) {
                var latVal = parseFloat(lat.value);
                if (isNaN(latVal) || latVal < -90 || latVal > 90) {
                    showError('latitude', 'Latitude must be between -90 and 90.');
                    valid = false;
                }
            }

            if (lng && lng.value) {
                var lngVal = parseFloat(lng.value);
                if (isNaN(lngVal) || lngVal < -180 || lngVal > 180) {
                    showError('longitude', 'Longitude must be between -180 and 180.');
                    valid = false;
                }
            }

            if (!valid) e.preventDefault();
        });
    }

    /* ----------------------------------------------------------
       Delete / Danger Confirmations
       ---------------------------------------------------------- */
    var dangerBtns = document.querySelectorAll('.btn-danger');
    dangerBtns.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            if (btn.tagName === 'A' || btn.type === 'submit') {
                if (!confirm('Are you sure? This action cannot be undone.')) {
                    e.preventDefault();
                }
            }
        });
    });

    /* ----------------------------------------------------------
       Unfavorite Confirmation
       ---------------------------------------------------------- */
    var unfavBtns = document.querySelectorAll('.btn-unfavorite');
    unfavBtns.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            if (!confirm('Remove this location from your favorites?')) {
                e.preventDefault();
            }
        });
    });

    /* ----------------------------------------------------------
       Flash Message Auto-Dismiss (fade out after 5 seconds)
       ---------------------------------------------------------- */
    var alerts = document.querySelectorAll('.alert');
    if (alerts.length > 0) {
        setTimeout(function () {
            alerts.forEach(function (alert) {
                alert.style.transition = 'opacity 0.5s ease';
                alert.style.opacity = '0';
                setTimeout(function () {
                    alert.remove();
                }, 500);
            });
        }, 5000);
    }

});
