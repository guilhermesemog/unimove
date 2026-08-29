/**
 * Active product copy. The interface is English-only for now.
 * Keeping shared language here prevents shell and common component labels from
 * drifting, and gives a future translation layer one stable integration point.
 */
export const UI_COPY = {
  brand: {
    name: 'UNIMOVE',
    tagline: 'University mobility, simplified.',
  },
  actions: {
    back: 'Go back',
    cancel: 'Cancel',
    close: 'Close',
    confirm: 'Confirm',
    clear: 'Clear',
    search: 'Search',
    signOut: 'Sign out',
  },
  navigation: {
    openMenu: 'Open navigation',
    closeMenu: 'Close navigation',
    student: {
      home: 'Home',
      bookTrip: 'Book a Trip',
      myTrips: 'My Trips',
      profile: 'Profile',
    },
    driver: {
      today: 'Today',
      schedule: 'Schedule',
      profile: 'Profile',
    },
    admin: {
      overview: 'Overview',
      operations: 'Operations',
      demand: 'Demand & Bookings',
      schedule: 'Trip Schedule',
      issues: 'Issues',
      people: 'People',
      users: 'Users',
      resources: 'Resources',
      universities: 'Universities',
      boardingStops: 'Boarding Stops',
      vehicles: 'Vehicles',
    },
  },
  states: {
    loading: 'Loading',
    loadingDescription: 'Getting the latest information for you.',
    empty: 'Nothing here yet',
    emptyDescription: 'New information will appear here when it becomes available.',
    error: 'Something went wrong',
    errorDescription: 'We could not load this information. Please try again.',
    retry: 'Try again',
  },
  pagination: {
    label: 'Pagination',
    itemsPerPage: 'Items per page:',
    previous: 'Previous',
    next: 'Next',
  },
  student: {
    home: {
      eyebrow: 'Your university commute',
      nextTrip: 'Your Next Trip',
      noTrip: 'No upcoming trip yet',
      noTripDescription: 'Find an available trip and reserve your seat in a few steps.',
      bookTrip: 'Book a Trip',
      viewTrips: 'View My Trips',
      upcoming: 'Coming Up',
    },
    availability: {
      title: 'Available Trips',
      description: 'Choose the date and schedule that work for you.',
      question: 'When do you need to travel?',
      empty: 'No available trips found',
      emptyDescription: 'Try another date or check back when new schedules are published.',
    },
    checkout: {
      title: 'Book Your Trip',
      tripType: 'Choose your trip',
      boardingStop: 'Boarding stop',
      review: 'Review your booking',
      confirm: 'Confirm Booking',
      confirming: 'Confirming...',
      success: 'Your booking is confirmed',
    },
    journeys: {
      title: 'My Trips',
      description: 'Your bookings and confirmed travel plans in one place.',
      upcoming: 'Upcoming',
      history: 'History',
      emptyUpcoming: 'No upcoming trips',
      emptyHistory: 'No trip history yet',
    },
    profile: {
      title: 'Profile',
      description: 'Your personal and student travel information.',
      preferredStop: 'Preferred Boarding Stop',
      saveStop: 'Save Boarding Stop',
      saved: 'Your preferred boarding stop has been updated.',
    },
  },
  admin: {
    overview: {
      title: 'Operations Overview',
      description: 'Monitor demand, capacity, and assignments across the operation.',
      needsAttention: 'Needs Attention',
      nextSevenDays: 'Next 7 Days',
      noAttention: 'No urgent operational issues',
      noAttentionDescription: 'Demand and assignments are currently in a healthy state.',
    },
    demand: {
      title: 'Demand & Bookings',
      description: 'Plan available dates and turn student demand into assigned trips.',
    },
    schedule: {
      title: 'Trip Schedule',
      description: 'Review capacity, assignments, and status for planned operations.',
    },
  },
} as const;
