import { Route, Routes } from "react-router-dom";
import "./App.css";
import GroupListPage from "./pages/Groups/GroupListPage";
import GroupAddPage from "./pages/Groups/GroupAddPage";

import "./index.css";

function App() {
  return (
    <Routes>
      {/* http://localhost:5173 */}
      <Route path="/" element={<GroupListPage />} />

      {/* http://localhost:5173/groups */}
      <Route path="/groups" element={<GroupListPage />} />

      {/* http://localhost:5173/add-group */}
      <Route path="/add-group" element={<GroupAddPage />} />
    </Routes>
  );
}

export default App;
