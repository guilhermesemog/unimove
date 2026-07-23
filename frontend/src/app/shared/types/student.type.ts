import { User } from "./user.type";

export interface Student {
    user: User;
    period: number;
    course: string;
    address: string;
    university: string;
    preferredBoardingStop: string;
}