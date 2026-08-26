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
} as const;
