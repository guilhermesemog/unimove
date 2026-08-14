import { BoardingStop } from "./boarding-stop.type";
import { University } from "./university.type";
import { User, UserCommomCreate, UserUpdateRequest } from "./user.type";

export interface Student {
    user: User;
    period: number;
    course: string;
    address: string;
    university: University;
    preferredBoardingStop: BoardingStop | null;
}

export interface StudentUpdateRequest {
    user: UserUpdateRequest;
    period: number;
    course: string;
    address: string;
    universityId: number;
    preferredBoardingStopId: number | null;
}

export interface StudentCreateRequest {
    user: UserCommomCreate;
    period: number;
    course: string;
    address: string;
    universityId: number;
    preferredBoardingStopId: number | null;
}