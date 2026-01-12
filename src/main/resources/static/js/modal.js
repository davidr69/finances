/**
 * Custom modal functionality to replace Bootstrap modal
 */
export default class CustomModal {
  constructor(modalId) {
    this.modal = document.getElementById(modalId);
    this.closeButtons = this.modal.querySelectorAll('.custom-close, .modal-close-btn');
    
    // Set up event listeners
    this.setupEventListeners();
  }

  setupEventListeners() {
    // Close button click handlers
    this.closeButtons.forEach(button => {
      button.addEventListener('click', () => this.close());
    });
    
    // Close when clicking outside the modal content
    this.modal.addEventListener('click', (event) => {
      if (event.target === this.modal) {
        this.close();
      }
    });
    
    // Close on escape key
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && this.isOpen()) {
        this.close();
      }
    });
  }

  open() {
    this.modal.style.opacity = '0';
    this.modal.style.display = 'block';
    fadeIn('myModal');
    document.body.style.overflow = 'hidden'; // Prevent scrolling of background
  }

  close() {
    this.modal.style.display = 'none';
    document.body.style.overflow = ''; // Restore scrolling
  }

  isOpen() {
    return this.modal.style.display === 'block';
  }
}