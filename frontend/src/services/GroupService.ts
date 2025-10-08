import { api } from "./client";
import type { Group, GroupCreate } from "../types";

export const listAllGroups = () => api.get<Group[]>("/groups");

export const createGroup = (group: GroupCreate) =>
  api.post<Group>("/groups", group);
