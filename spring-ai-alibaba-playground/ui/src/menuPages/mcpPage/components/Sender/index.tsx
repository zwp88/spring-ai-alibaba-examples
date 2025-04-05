// import React, { useState } from "react";
// import { useStyles } from "../../styles";

// interface SenderProps {
//   onSendMessage: (message: string) => void;
// }

// const Sender: React.FC<SenderProps> = ({ onSendMessage }) => {
//   const [message, setMessage] = useState("");
//   const { styles } = useStyles();

//   const handleSubmit = (e: React.FormEvent) => {
//     e.preventDefault();
//     if (message.trim()) {
//       onSendMessage(message);
//       setMessage("");
//     }
//   };

//   return (
//     <div className={`${styles.card}`}>
//       <h2 className={styles.panelTitle}>Send Message</h2>
//       <form onSubmit={handleSubmit} className={styles.messageForm}>
//         <textarea
//           className={styles.messageInput}
//           placeholder="Type your message here..."
//           value={message}
//           onChange={(e) => setMessage(e.target.value)}
//         />
//         <button
//           type="submit"
//           className={styles.sendButton}
//           disabled={!message.trim()}
//         >
//           Send
//         </button>
//       </form>
//     </div>
//   );
// };

// export default Sender;
