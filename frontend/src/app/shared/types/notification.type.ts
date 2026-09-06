export type NotificationType = 'BOOKING' | 'DEMAND' | 'TRIP' | 'ASSIGNMENT' | 'REMINDER' | 'ISSUE';
export type NotificationCategory = 'INFO' | 'ACTION' | 'REMINDER' | 'ISSUE';

export interface AppNotification {
  id: string;
  type: NotificationType;
  category: NotificationCategory;
  contentKey: string;
  title: string;
  description: string;
  route: string;
  createdAt: string;
  readAt: string | null;
}
