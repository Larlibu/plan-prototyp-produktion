// General JavaScript for the application
// Currently empty, add any global scripts here.

document.addEventListener('DOMContentLoaded', function() {
    // Example: Simple fade out for alerts after some time
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease-out';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500); // Remove from DOM after fade
        }, 5000); // 5 seconds
    });

    // Example for image preview in forms (if needed)
    const imageFileInput = document.getElementById('imageFile');
    if (imageFileInput) {
        imageFileInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    let previewImage = document.querySelector('.current-image img');
                    if (!previewImage) {
                        // Create preview if it doesn't exist
                        const previewContainer = document.createElement('div');
                        previewContainer.className = 'current-image';
                        previewContainer.innerHTML = '<p>Vorschau:</p><img style="max-width: 200px; height: auto;">';
                        imageFileInput.parentNode.insertBefore(previewContainer, imageFileInput.nextSibling);
                        previewImage = previewContainer.querySelector('img');
                    }
                    previewImage.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }
});
