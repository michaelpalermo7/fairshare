export interface UserCreate {
  userName: string;
  userEmail: string;
}

export interface User {
  userId: number;
  userName: string;
  userEmail: string;
}

export interface GroupCreate {
  name: string;
  creatorUserId: number;
}

export interface Group {
  id: number;
  name: string;
  createdAt: string;
}
