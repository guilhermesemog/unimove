import { User } from "./user.type";

export interface Conductor {
    user: User;
    license: string;
    licenseExpirationDate: Date;
}