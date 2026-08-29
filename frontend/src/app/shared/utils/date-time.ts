import { InterestList } from '../types/interest-list.type';

export function parseLocalDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function combineLocalDateTime(date: string, time: string): Date {
  const result = parseLocalDate(date);
  const [hours, minutes] = time.split(':').map(Number);
  result.setHours(hours, minutes, 0, 0);
  return result;
}

export function formatDate(value: string, options?: Intl.DateTimeFormatOptions): string {
  return new Intl.DateTimeFormat('en-US', options ?? {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  }).format(parseLocalDate(value));
}

export function formatTime(value: string): string {
  const [hours, minutes] = value.split(':').map(Number);
  const date = new Date(2000, 0, 1, hours, minutes);
  return new Intl.DateTimeFormat('en-US', { hour: 'numeric', minute: '2-digit' }).format(date);
}

export function relativeDateLabel(value: string, now = new Date()): string {
  const date = parseLocalDate(value);
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const difference = Math.round((date.getTime() - today.getTime()) / 86_400_000);

  if (difference === 0) return 'Today';
  if (difference === 1) return 'Tomorrow';
  if (difference === -1) return 'Yesterday';
  return formatDate(value);
}

export function isBookingOpen(interestList: InterestList): boolean {
  return interestList.listStatus === 'OPEN';
}

export function isUpcomingDate(value: string, now = new Date()): boolean {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  return parseLocalDate(value).getTime() >= today.getTime();
}
