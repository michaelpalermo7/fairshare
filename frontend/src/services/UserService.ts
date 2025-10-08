import type { User, UserCreate } from "../types";
import { api } from "./client";

export const createUser = (user: UserCreate) => api.post<User>("/users", user);
