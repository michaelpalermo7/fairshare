import { useEffect, useState } from "react";
import { listAllGroups } from "../services/GroupService";
import type { Group } from "../types";
import { useNavigate } from "react-router-dom";

const Table = () => {
  //state to update the table with groups
  //using <Group[]> to signify type (found in src/types.ts)
  const [groups, setGroups] = useState<Group[]>([]);

  //formatting date of createdAt since we used Instant in the backend (which isnt formatted nicely for the user)
  const fmtDate = (iso: string) => new Date(iso).toLocaleDateString("en-CA");

  //fetches response of api and sets state setGroups, uses the api call in services/GroupService.ts
  useEffect(() => {
    listAllGroups()
      .then((response) => {
        setGroups(response.data);
      })
      .catch((error) => {
        console.error(error);
      });
  }, []);

  //on buttoo click, user goes to add group page
  const navigator = useNavigate();
  function addNewGroup() {
    navigator("/add-group");
  }

  return (
    <div className="relative overflow-x-auto shadow-md sm:rounded-lg bg-white">
      <h2 className="text-3xl font-semibold text-gray-800 p-4">
        List Of Groups
      </h2>

      <button
        type="button"
        onClick={addNewGroup}
        className="cursor-pointer float-left text-white bg-blue-700 hover:bg-blue-800  focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800"
      >
        Add Group
      </button>

      <table className="w-full text-lg text-left rtl:text-right text-gray-700">
        <thead className="text-md text-gray-800 uppercase bg-gray-100">
          <tr>
            <th scope="col" className="px-6 py-3">
              Group Id
            </th>
            <th scope="col" className="px-6 py-3">
              Group Name
            </th>
            <th scope="col" className="px-6 py-3">
              Group Created At
            </th>
          </tr>
        </thead>

        <tbody>
          {groups.map((group, index) => (
            <tr
              key={group.id}
              className={`${
                index % 2 === 0 ? "bg-white" : "bg-gray-100"
              } border-none`}
            >
              <td className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap">
                {group.id}
              </td>
              <td className="px-6 py-4">{group.name}</td>
              <td className="px-6 py-4">{fmtDate(group.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
