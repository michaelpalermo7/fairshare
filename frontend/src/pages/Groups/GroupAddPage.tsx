import { useState, type FormEvent, type ChangeEvent } from "react";
import { createUser } from "../../services/UserService";
import { createGroup } from "../../services/GroupService";

const GroupAddPage = () => {
  const [name, setName] = useState<string>("");
  const [userName, setUserName] = useState<string>("");
  const [userEmail, setUserEmail] = useState<string>("");

  //logic for group and user creation
  const saveGroup = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    //create user, and get response data that has user id
    const { data: newUser } = await createUser({
      userName,
      userEmail,
    });
    console.log(newUser.userId);

    //use that new user id to create a group with that user in it as admin
    const { data: newGroup } = await createGroup({
      name,
      creatorUserId: newUser.userId,
    });

    console.log("Created user:", newUser);
    console.log("Created group:", newGroup);
  };

  return (
    <div className="container mx-auto px-4">
      <br />
      <br />
      <div className="flex justify-center">
        <div className="w-full md:w-1/2 bg-white shadow-lg rounded-lg">
          <div className="p-6">
            <h2 className="text-center text-2xl font-semibold mb-6">
              Add Group
            </h2>

            {/* Form */}
            <form onSubmit={saveGroup}>
              {/* Group name */}
              <div className="mb-4">
                <label className="block text-gray-700 text-sm font-medium mb-2">
                  Group Name
                </label>
                <input
                  type="text"
                  className="w-full border border-gray-300 rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter group name"
                  value={name}
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setName(e.target.value)
                  }
                  autoComplete="organization"
                  required
                />
              </div>

              {/* Admin name */}
              <div className="mb-4">
                <label className="block text-gray-700 text-sm font-medium mb-2">
                  Admin Name
                </label>
                <input
                  type="text"
                  className="w-full border border-gray-300 rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter admin name"
                  value={userName}
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setUserName(e.target.value)
                  }
                  autoComplete="name"
                  required
                />
              </div>

              {/* Admin email */}
              <div className="mb-6">
                <label className="block text-gray-700 text-sm font-medium mb-2">
                  Admin Email
                </label>
                <input
                  type="email"
                  className="w-full border border-gray-300 rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter admin email"
                  value={userEmail}
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setUserEmail(e.target.value)
                  }
                  autoComplete="email"
                  required
                />
              </div>

              <div className="flex justify-center">
                <button
                  type="submit"
                  className="cursor-pointer bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
                >
                  Add Group
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GroupAddPage;
